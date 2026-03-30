package com.crud.config;

import java.util.Map;

import com.crud.controller.ItemController;
import com.crud.repository.ItemRepository;
import com.crud.repository.PostgreSQLItemRepository;
import com.crud.service.ItemService;
import io.javalin.Javalin;

public class AppConfig {
  private final ItemRepository repository;
  private final ItemService service;
  private final ItemController controller;
  private Javalin app;

  public AppConfig() {
    boolean usePostgres = System.getenv("DB_HOST") != null;
    this.repository = usePostgres ? new PostgreSQLItemRepository() : new ItemRepository();
    this.service = new ItemService(repository);
    this.controller = new ItemController(service);
  }

  public Javalin createApp() {
    app = Javalin.create(config -> {
      config.http.defaultContentType = "application/json";

      config.bundledPlugins.enableCors(cors -> {
        cors.addRule(it -> {
          it.allowHost("http://localhost:3000");
          it.allowCredentials = true;
        });
      });
    }).exception(Exception.class, (e, ctx) -> {
      e.printStackTrace();
      ctx.status(500).json(Map.of(
          "error", e.getClass().getSimpleName() + ": " + e.getMessage()));
    });

    setupRoutes();

    return app;
  }

  private void setupRoutes() {
    app.get("/api/health", controller::healthCheck);
    app.get("/api/items", controller::getAllItems);
    app.get("/api/items/{id}", controller::getItemById);
    app.post("/api/items", controller::createItem);
    app.put("/api/items/{id}", controller::updateItem);
    app.delete("/api/items/{id}", controller::deleteItem);
  }

  public ItemService getService() {
    return service;
  }

  public ItemRepository getRepository() {
    return repository;
  }

  public void start(int port) {
    if (app == null) {
      createApp();
    }
    app.start(port);
  }

  public void stop() {
    if (app != null) {
      app.stop();
    }
  }
}
