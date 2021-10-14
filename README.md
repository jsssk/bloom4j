# Bloom4j

## QuickStart

### Import Maven dependency
```xml
<dependency>
    <groupId>io.github.jsssk</groupId>
    <artifactId>bloom4j</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```
### Invoke

#### Default
```java
    long numOfItems = 100_000_000;
    double fpp = 1e-4;
    BloomFilterParam param = BloomFilterParam.of(numOfItems, fpp);
    BloomFilter bloomFilter = DefaultBloomFilter.create(param);
    
    bloomFilter.put("foo".getBytes(StandardCharsets.UTF_8));

    bloomFilter.contains("foo".getBytes(StandardCharsets.UTF_8));
```


#### Scalable
```java
    long numOfItems = 100_000_000;
    double fpp = 1e-4;
    long slotSize = 1 << 23; // 1 KB
    
    SlotStrategy slotStrategy = DefaultSlotStrategy.of(numOfItems, fpp, slotSize);
    BloomFilterHashStrategy strategy = BloomFilterHashStrategies.MURMUR128_MITZ_64;

    DefaultSlotBloomFilter.create(slotStrategy, strategy, (i, p) -> DefaultBloomFilter.create(strategy, p));
    ...
```
