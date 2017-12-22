package xyz.jetdrone.blockchain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class Block {

  private int index;
  private long timestamp;
  private String data;
  private int nonce;
  private String previousHash;

  public Block() {}

  public Block(JsonObject json) {
    BlockConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    BlockConverter.toJson(this, json);
    return json;
  }

  public int getIndex() {
    return index;
  }

  public Block setIndex(int index) {
    this.index = index;
    return this;
  }

  public String getPreviousHash() {
    return previousHash;
  }

  public Block setPreviousHash(String previousHash) {
    this.previousHash = previousHash;
    return this;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public Block setTimestamp(long timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  public String getData() {
    return data;
  }

  public Block setData(String data) {
    this.data = data;
    return this;
  }

  public int getNonce() {
    return nonce;
  }

  public Block setNonce(int nonce) {
    this.nonce = nonce;
    return this;
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}
