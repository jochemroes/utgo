package com.google.ar.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.resources.ResourceHolder;
import com.google.ar.sceneform.resources.ResourceRegistry;

import java.util.ArrayList;

/**
 * Minimal resource manager. Maintains mappings from ids to created resources and a task executor
 * dedicated to loading resources asynchronously.
 *
 * @hide
 */
@SuppressWarnings("initialization") // Suppress @UnderInitialization warning.
public class ResourceManager {
  @Nullable private static ResourceManager instance = null;

  private final ArrayList<ResourceHolder> resourceHolders = new ArrayList<>();
  private final ResourceRegistry<Texture> textureRegistry = new ResourceRegistry<>();
  private final ResourceRegistry<Material> materialRegistry = new ResourceRegistry<>();
  private final ResourceRegistry<ModelRenderable> modelRenderableRegistry =
      new ResourceRegistry<>();

  
  private final ResourceRegistry<ViewRenderable> viewRenderableRegistry = new ResourceRegistry<>();

  private final CleanupRegistry<CameraStream> cameraStreamCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<ExternalTexture> externalTextureCleanupRegistry =
      new CleanupRegistry<>();
  private final CleanupRegistry<Material> materialCleanupRegistry = new CleanupRegistry<>();
  private final CleanupRegistry<RenderableInstance> renderableInstanceCleanupRegistry =
      new CleanupRegistry<>();
  private final CleanupRegistry<Texture> textureCleanupRegistry = new CleanupRegistry<>();

  ResourceRegistry<Texture> getTextureRegistry() {
    return textureRegistry;
  }

  ResourceRegistry<Material> getMaterialRegistry() {
    return materialRegistry;
  }

  ResourceRegistry<ModelRenderable> getModelRenderableRegistry() {
    return modelRenderableRegistry;
  }

  
  ResourceRegistry<ViewRenderable> getViewRenderableRegistry() {
    return viewRenderableRegistry;
  }

  CleanupRegistry<CameraStream> getCameraStreamCleanupRegistry() {
    return cameraStreamCleanupRegistry;
  }

  CleanupRegistry<ExternalTexture> getExternalTextureCleanupRegistry() {
    return externalTextureCleanupRegistry;
  }

  CleanupRegistry<Material> getMaterialCleanupRegistry() {
    return materialCleanupRegistry;
  }

  CleanupRegistry<RenderableInstance> getRenderableInstanceCleanupRegistry() {
    return renderableInstanceCleanupRegistry;
  }

  CleanupRegistry<Texture> getTextureCleanupRegistry() {
    return textureCleanupRegistry;
  }

  public long reclaimReleasedResources() {
    long resourcesInUse = 0;
    for (ResourceHolder registry : resourceHolders) {
      resourcesInUse += registry.reclaimReleasedResources();
    }
    return resourcesInUse;
  }

  /** Forcibly deletes all tracked references */
  public void destroyAllResources() {
    for (ResourceHolder resourceHolder : resourceHolders) {
      resourceHolder.destroyAllResources();
    }
  }

  public void addResourceHolder(ResourceHolder resource) {
    resourceHolders.add(resource);
  }

  public static ResourceManager getInstance() {
    if (instance == null) {
      instance = new ResourceManager();
    }

    return instance;
  }

  private ResourceManager() {
    addResourceHolder(textureRegistry);
    addResourceHolder(materialRegistry);
    addResourceHolder(modelRenderableRegistry);
    addViewRenderableRegistry();
    addResourceHolder(cameraStreamCleanupRegistry);
    addResourceHolder(externalTextureCleanupRegistry);
    addResourceHolder(materialCleanupRegistry);
    addResourceHolder(renderableInstanceCleanupRegistry);
    addResourceHolder(textureCleanupRegistry);
  }

  
  private void addViewRenderableRegistry() {
    addResourceHolder(viewRenderableRegistry);
  }
}
