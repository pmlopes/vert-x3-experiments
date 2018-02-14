package xyz.jetdrone.detour.impl;

import io.vertx.core.MultiMap;

public interface Context<V> {

  V getVerb();

  String getPath();

  Context<V> addParam(String key, String value);
}
