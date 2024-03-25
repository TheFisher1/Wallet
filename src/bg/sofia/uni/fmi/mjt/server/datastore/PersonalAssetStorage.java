package bg.sofia.uni.fmi.mjt.server.datastore;

import bg.sofia.uni.fmi.mjt.server.dto.Currency;
import bg.sofia.uni.fmi.mjt.server.exceptions.AssetNotFoundException;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmptyMarketException;
import bg.sofia.uni.fmi.mjt.server.financials.PersonalWallet;
import bg.sofia.uni.fmi.mjt.server.utils.TextProcessor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PersonalAssetStorage {
    private final TextProcessor textProcessor;
    private String filename;
    private final Map<String, PersonalWallet> map = new LinkedHashMap<>();

    public PersonalAssetStorage(Collection<String> keys, String filename) {
        textProcessor = new TextProcessor();
        this.filename = filename;

        try (FileReader fileReader = new FileReader(filename)) {
            Map<String, PersonalWallet> assets = textProcessor.loadPersonalAssets(fileReader);
            if (assets.isEmpty()) {
                keys.stream().forEach(s -> map.put(s, new PersonalWallet()));
            }
            this.map.putAll(assets);

        } catch (IOException e) {
            for (String s : keys) {
                map.put(s, new PersonalWallet());
            }
        }
    }

    public PersonalAssetStorage(Map<String, PersonalWallet> map) {
        this.textProcessor = new TextProcessor();
        this.map.putAll(map);
    }

    public void save(Writer writer) {
        textProcessor.savePersonalAssets(writer, map);
    }

    public void saveAll(Writer writer) {
        textProcessor.savePersonalAssets(writer, map);
    }

    public Map<String, PersonalWallet> load(Reader reader) {
        return textProcessor.loadPersonalAssets(reader);
    }

    public void add(String key, PersonalWallet value) {
        map.put(key, value);
    }

    public PersonalWallet get(String key) {
        map.putIfAbsent(key, new PersonalWallet());
        return map.get(key);
    }

    public void buyAsset(String username, double amount, String assetId, Map<String, Currency> assets)
        throws AssetNotFoundException {

        map.putIfAbsent(username, new PersonalWallet());
        PersonalWallet wallet = map.get(username);

        if (assets == null) {
            throw new EmptyMarketException("no currencies are offered");
        }

        if (assets.get(assetId) == null) {
            throw new AssetNotFoundException("no asset with id: " + assetId + " is currently available");
        }

        wallet.acquireAsset(amount, assetId, assets);
        map.put(username, wallet);
    }

    public void sellAsset(String username, String assetId, Map<String, Currency> assetPrices)
        throws AssetNotFoundException {
        PersonalWallet wallet = map.get(username);

        if (wallet == null) {
            throw new AssetNotFoundException("asset with " + assetId + " could not be found");
        }

        wallet.sellAsset(assetId, assetPrices);
        map.put(username, wallet);
    }

    public Collection<String> getKeys() {
        return Collections.unmodifiableCollection(map.keySet());
    }

    public Map<String, PersonalWallet> getAll() {
        return Collections.unmodifiableMap(this.map);
    }

}
