package bg.sofia.uni.fmi.mjt.server.dto;

import com.google.gson.annotations.SerializedName;

public record Currency(@SerializedName("asset_id") String assetId,
                       @SerializedName("name") String name,
                       @SerializedName("price_usd") double priceUsd,
                       @SerializedName("type_is_crypto") int isCrypto) {

    @Override
    public int hashCode() {
        return assetId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Currency)) {
            return false;
        }
        return assetId.equals(((Currency)o).assetId);
    }

}
