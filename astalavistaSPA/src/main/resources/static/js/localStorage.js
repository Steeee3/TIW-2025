const TTL = 24 * 30 * 60 * 60 * 1000;
const STACK_MAX_SIZE = 20;

let userKey = "guest";
const namespace = (k) => `${userKey}:${k}`;

const stackKeys = {
    sizeKey: namespace("stackSize"),
    indexKey: namespace("stackIndex"),
    dataKey: namespace("stackData")
}

class Stack {
    constructor(size, index, data) {
        this.size = size;
        this.index = index;
        this.data = data;
    }

    _save() {
        setLocalStorage(stackKeys.sizeKey, this.size);
        setLocalStorage(stackKeys.indexKey, this.index);
        setLocalStorage(stackKeys.dataKey, this.data);
    }

    enqueue(value) {
        if (this.index + 1 == STACK_MAX_SIZE) {
            this.index = -1;
        }
        this.data[++this.index] = value;
        if (this.size < STACK_MAX_SIZE) {
            this.size++;
        }

        this._save();
    }

    dequeue() {
        if (this.size == 0) {
            return null;
        }
        const value = this.data[this.index];
        this.data[this.index--] = null;
        this.size--;

        this._save();
        return value;
    }

    peek() {
        if (this.size == 0) {
            return null;
        }
        return this.data[this.index];
    }

    getAllElements() {
        let elements = [];

        for (let i = 0; i < this.size; i++) {
            elements[i] = this.data[i];
        }

        return elements;
    }

    rearrageElements() {
        let oldSize = this.size;
        this.data = this.data.filter(e => e != null);
        this.size = this.data.length;

        if (this.size != oldSize) {
            this.index = this.size - 1;
        }

        this._save();
    }

    contains(value) {
        return this.data.includes(value);
    }
}

function loadStack() {
    const stackSize = getLocalStorageValue(stackKeys.sizeKey) ?? 0;
    const stackIndex = getLocalStorageValue(stackKeys.indexKey) ?? -1;
    const stackData = getLocalStorageValue(stackKeys.dataKey) ?? [];
    return new Stack(stackSize, stackIndex, stackData);
}

let auctionStack = loadStack();

function addRecentlyViewedAuction(auctionId) {
    let key = namespace(`auction${auctionId}`);

    if (auctionStack.contains(key)) {
        return;
    }
    auctionStack.enqueue(key);
    setLocalStorage(auctionStack.peek(), auctionId);
}

function getAllRecentlyViewedAuctionIds() {
    const keys = auctionStack.getAllElements();

    let ids = [];
    for (let i = 0; i < keys.length; i++) {
        ids[i] = getLocalStorageValue(keys[i]);

        if (ids[i] == null) {
            auctionStack.data[i] = null;
        }
    }
    auctionStack.rearrageElements();
    return ids.filter(e => e != null);
}

function setLocalStorage(key, value) {
    const record = {
        value: value,
        timestamp: Date.now(),
        ttl: TTL
    };

    localStorage.setItem(key, JSON.stringify(record));
}

function getLocalStorageValue(key) {
    const data = localStorage.getItem(key);

    if (!data) {
        return null;
    }

    try {
        const record = JSON.parse(data);

        if (Date.now() - record.timestamp > record.ttl) {
            localStorage.removeItem(key);

            return null;
        }

        return record.value;
    } catch {
        localStorage.removeItem(key);

        return null;
    }
}

function initLocalStorage(u) {
  userKey = u || "guest";

  stackKeys.sizeKey = namespace("stackSize");
  stackKeys.indexKey = namespace("stackIndex");
  stackKeys.dataKey =  namespace("stackData");

  auctionStack = loadStack();
}