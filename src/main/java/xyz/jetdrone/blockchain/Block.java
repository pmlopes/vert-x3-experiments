package xyz.jetdrone.blockchain;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject(generateConverter = true)
public class Block {

  public static final Block GENESIS = new Block()
    .setIndex(0)
    .setPreviousHash("0")
    .setTimestamp(1501122600)
    .setData("<genesis>")
    .setHash("0000018035a828da0878ae92ab6fbb16be1ca87a02a3feaa9e3c2b6871931046")
    .setNonce(56551);

  private int index = 0;
  private String previousHash = "0";
  private long timestamp = System.currentTimeMillis() / 1000;
  private String data = "none";
  private String hash = "";
  private int nonce = 0;

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

  public String getHash() {
    return hash;
  }

  public Block setHash(String hash) {
    this.hash = hash;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Block block = (Block) o;

    if (index != block.index) return false;
    if (timestamp != block.timestamp) return false;
    if (nonce != block.nonce) return false;
    if (previousHash != null ? !previousHash.equals(block.previousHash) : block.previousHash != null) return false;
    if (data != null ? !data.equals(block.data) : block.data != null) return false;
    return hash != null ? hash.equals(block.hash) : block.hash == null;
  }

  @Override
  public int hashCode() {
    int result = index;
    result = 31 * result + (previousHash != null ? previousHash.hashCode() : 0);
    result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
    result = 31 * result + (data != null ? data.hashCode() : 0);
    result = 31 * result + (hash != null ? hash.hashCode() : 0);
    result = 31 * result + nonce;
    return result;
  }
}
