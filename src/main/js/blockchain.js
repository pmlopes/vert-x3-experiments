import EventBus from 'vertx3-eventbus-client';
import sha256 from 'sha256';

export class Blockchain {
  constructor (gateway, address) {
    this.eb = new EventBus('//' + gateway + '/eventbus', {
      vertxbus_reconnect_attempts_max: Infinity, // Max reconnect attempts
      vertxbus_reconnect_delay_min: 1000, // Initial delay (in ms) before first reconnect attempt
      vertxbus_reconnect_delay_max: 5000, // Max delay (in ms) between reconnect attempts
      vertxbus_reconnect_exponent: 2, // Exponential backoff factor
      vertxbus_randomization_factor: 0.5 // Randomization factor between 0 and 1
    });
    this.eb.enableReconnect(true);
    this.address = address;

    this.chain = [];
    // insert the genesis block
    this.chain.push({
      index: 0,
      nonce: 1,
      timestamp: 0,
      previousHash: '',
      data: '<genesis>'
    });
  }

  connect(onConnect) {
    let eb = this.eb;
    let self = this;

    eb.onopen = () => {
      eb.send(self.address, null, (err, message) => {
        if (!err) {
          this.consensus(message.body);
        }
      });
      eb.registerHandler(self.address, (err, message) => {
        if (err) {
          return console.error(err);
        }

        if (message.body) {
          this.consensus([message.body]);
        } else {
          message.reply(self.chain);
        }
      });

      onConnect();
    };
  }

  disconnect(onDisconnect) {
    this.eb.unregisterHandler(self.address, onDisconnect);
  }

  onBlock(callback) {
    this.onBlockCb = callback;
  }

  onReplace(callback) {
    this.onReplaceCb = callback;
  }

  size() {
    return this.chain.length;
  }

  get(index) {
    if (index < 0 || index >= this.size()) {
      return null;
    }

    return this.chain[index];
  }

  last() {
    return this.chain[this.size() - 1];
  }

  add(data, onAdd) {
    // we run the proof of work algorithm to get the next proof...
    let lastBlock = this.last();
    let lastProof = lastBlock.nonce;
    let proof = this.proofOfWork(lastProof);

    // forge the new Block by adding it to the chain
    let block = {
      index: this.size(),
      timestamp: Date.now(),
      data: data,
      nonce: proof,
      previousHash: this.hash(lastBlock)
    };

    this.chain.push(block);
    if (this.onBlockCb) {
      this.onBlockCb(this.chain);
    }
    // announce to peers the new mined block
    this.eb.publish(this.address, block, onAdd);
    return this;
  }

  /**
   * Creates a SHA-256 hash of a Block
   *
   * @param block the block to encode
   * @return sha256 hex value
   */
  hash(block) {
    return sha256(block.index + ":" + block.previousHash + ":" + block.timestamp + ":" + block.data + ":" + block.nonce);
  }

  /**
   * Simple Proof of Work Algorithm:
   * - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is the previous p'
   * - p is the previous proof, and p' is the new proof
   *
   * @param {Number} lastProof the previous proof
   * @return {Number} the current proof
   */
  proofOfWork(lastProof) {
    let proof = 0;
    while (!this.validProof(lastProof, proof)) {
      proof++;
    }

    return proof;
  }

  /**
   * Validates the Proof. (Implementation detail)
   *
   * @param {Number} lastProof Previous proof
   * @param {Number} proof     Current proof
   * @return {Boolean} true if correct, false if not
   */
  validProof(lastProof, proof) {
    let guess = lastProof + '' + proof;
    let guessHash = sha256(guess);
    // hardcoded difficulty of 4
    return guessHash.substring(0, 4) === '0000';
  }


  /**
   * Determine if a given blockchain is valid.
   *
   * @param {Array} chain A blockchain
   * @return {Boolean} True if valid, False if not
   */
  validChain(chain) {
    let lastBlock = chain[0];
    let currentIndex = 1;

    while (currentIndex < chain.length) {
      let block = chain[currentIndex];
      // check that the hash of the block is correct
      if (block.previousHash !== this.hash(lastBlock)) {
        return false;
      }
      // check that the Proof of Work is correct
      if (!this.validProof(lastBlock.nonce, block.nonce)) {
        return false;
      }

      lastBlock = block;
      currentIndex++;
    }

    return true;
  }

  /**
   * This is our consensus algorithm, it resolves conflicts
   * by replacing our chain with the longest one in the network.
   */
  consensus(receivedBlocks) {
    let latestBlockReceived = receivedBlocks[receivedBlocks.length - 1];
    let latestBlockHeld = this.last();

    if (latestBlockReceived.index <= latestBlockHeld.index) {
      // received latest block is not longer than current store. Do nothing
      return;
    }

    if (this.hash(latestBlockHeld) === latestBlockReceived.previousHash) {
      // previous hash received is equal to current hash. Append received block to store.
      this.chain.push(latestBlockReceived);
      if (this.onBlockCb) {
        this.onBlockCb(this.chain);
      }

    } else if (receivedBlocks.length === 1) {
      // received previous hash different from current hash. Get entire store from peer.
      this.eb.send(this.address, null, (err, message) => {
        if (!err) {
          this.consensus(message.body);
        }
      });
    } else {
      // Peer store is longer than current store.
      if (this.validChain(receivedBlocks)) {
        this.chain = receivedBlocks;
        if (this.onReplaceCb) {
          this.onReplaceCb(this.chain);
        }
      }
    }
  }
}
