package xyz.apex.minecraft.apexcore.forge.data;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public abstract class AbstractBlockBenchConverter implements DataProvider
{
    protected static final Logger LOGGER = LogManager.getLogger();

    protected final DataGenerator generator;
    protected final ExistingFileHelper existingFileHelper;
    protected final String modId;
    protected final BlockModelProvider blockModels;
    protected final ItemModelProvider itemModels;
    private final Collection<Path> inputFolders;

    private final Map<ResourceLocation, BlockModelBuilder> blockModelBuilders = Maps.newHashMap();
    private final Map<ResourceLocation, Path> blockModelInputPaths = Maps.newHashMap();
    private final Map<ResourceLocation, ItemModelBuilder> itemModelBuilders = Maps.newHashMap();
    private final Map<ResourceLocation, Path> itemModelInputPaths = Maps.newHashMap();

    protected AbstractBlockBenchConverter(GatherDataEvent event, String modId)
    {
        this(event, null, null, modId);
    }

    protected AbstractBlockBenchConverter(GatherDataEvent event, @Nullable BlockModelProvider blockModels, @Nullable ItemModelProvider itemModels, String modId)
    {
        generator = event.getGenerator();
        existingFileHelper = event.getExistingFileHelper();
        inputFolders = getDataGeneratorConfig(event).getInputs();

        this.modId = modId;
        this.blockModels = blockModels == null ? dummyBlockModels(generator, existingFileHelper, modId) : blockModels;
        this.itemModels = itemModels == null ? dummyItemModels(generator, existingFileHelper, modId) : itemModels;
    }

    protected AbstractBlockBenchConverter(GatherDataEvent event, @Nullable BlockModelProvider blockModels, String modId)
    {
        this(event, blockModels, null, modId);
    }

    protected AbstractBlockBenchConverter(GatherDataEvent event, @Nullable ItemModelProvider itemModels, String modId)
    {
        this(event, null, itemModels, modId);
    }

    protected abstract void convertModels();

    public final BlockModelBuilder blockModelBuilder(ResourceLocation inputPath, ResourceLocation outputPath)
    {
        return blockModelBuilders.computeIfAbsent(inputPath, $ ->
                BlockBenchModelDeserializer.blockModelBuilder(
                        outputPath,
                        blockModelInputPaths.computeIfAbsent(inputPath, this::findInputPath),
                        existingFileHelper
                )
        );
    }

    public final BlockModelBuilder blockModelBuilder(ResourceLocation modelPath)
    {
        return blockModelBuilder(modelPath, modelPath);
    }

    public final ItemModelBuilder itemModelBuilder(ResourceLocation inputPath, ResourceLocation outputPath)
    {
        return itemModelBuilders.computeIfAbsent(inputPath, $ ->
                BlockBenchModelDeserializer.itemModelBuilder(
                        outputPath,
                        itemModelInputPaths.computeIfAbsent(inputPath, this::findInputPath),
                        existingFileHelper
                )
        );
    }

    public final ItemModelBuilder itemModelBuilder(ResourceLocation modelPath)
    {
        return itemModelBuilder(modelPath, modelPath);
    }

    public final BlockModelProvider blockModels()
    {
        return blockModels;
    }

    public final ItemModelProvider itemModels()
    {
        return itemModels;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output)
    {
        blockModelBuilders.clear();
        itemModelBuilders.clear();

        convertModels();
        var outputDir = generator.getPackOutput().getOutputFolder(PackOutput.Target.RESOURCE_PACK);

        return CompletableFuture.allOf(Stream
                .concat(blockModelBuilders.entrySet().stream(), itemModelBuilders.entrySet().stream())
                .map(entry -> generateModel(output, outputDir, entry.getKey(), entry.getValue()))
                .toArray(CompletableFuture[]::new)
        );
    }

    private CompletableFuture<?> generateModel(CachedOutput output, Path outputDir, ResourceLocation inputPath, ModelBuilder<?> modelBuilder)
    {
        var json = modelBuilder.toJson();
        var outputPath = modelBuilder.getUncheckedLocation();
        LOGGER.info("Generating converted BlockBench model '{}' -> '{}'", inputPath, outputPath);
        var filePath = buildModelPath(outputDir, outputPath);
        existingFileHelper.trackGenerated(outputPath, PackType.CLIENT_RESOURCES, ".json", "models");
        return DataProvider.saveStable(output, json, filePath);
    }

    @Override
    public String getName()
    {
        return "BlockBench2Minecraft-Converter";
    }

    private Path buildModelPath(Path dir, ResourceLocation modelPath)
    {
        return dir
                .resolve(modelPath.getNamespace())
                .resolve("models")
                .resolve("%s.json".formatted(modelPath.getPath()))
        ;
    }

    private Path findInputPath(ResourceLocation modelPath)
    {
        for(var inputDir : inputFolders)
        {
            var filePath = buildModelPath(inputDir, modelPath);
            if(Files.exists(filePath)) return filePath;
        }

        throw new RuntimeException(new FileNotFoundException("Could not determine input model path from model name '%s'".formatted(modelPath)));
    }

    public static BlockModelProvider dummyBlockModels(DataGenerator generator, ExistingFileHelper existingFileHelper, String modId)
    {
        return new BlockModelProvider(generator, modId, existingFileHelper) {
            @Override
            protected void registerModels()
            {
            }
        };
    }

    public static ItemModelProvider dummyItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper, String modId)
    {
        return new ItemModelProvider(generator, modId, existingFileHelper) {
            @Override
            protected void registerModels()
            {
            }
        };
    }

    private static GatherDataEvent.DataGeneratorConfig getDataGeneratorConfig(GatherDataEvent event)
    {
        try
        {
            var field = GatherDataEvent.class.getDeclaredField("config");
            field.setAccessible(true);
            return (GatherDataEvent.DataGeneratorConfig) field.get(event);
        }
        catch(NoSuchFieldException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }
}
