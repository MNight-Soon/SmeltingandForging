package org.mnight.smeltingandforging.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class HeatHandler implements INBTSerializable<CompoundTag> {
    private int temperature;
    private int maxTemperature;
    private int minTemperature;

    public HeatHandler(int maxTemperature) {
        this(0,maxTemperature);
    }

    public HeatHandler(int minTemperature, int maxTemperature) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.temperature = minTemperature;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = Math.max(minTemperature, Math.min(temperature, maxTemperature));
    }

    public void heatUp(int amount) {
        setTemperature(this.temperature + amount);
    }

    public void coolDown(int amount) {
        setTemperature(this.temperature - amount);
    }

    public boolean isHotEnough(int requiredTemp) {
        return this.temperature >= requiredTemp;
    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Temperature", this.temperature);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("Temperature")) {
            this.temperature = nbt.getInt("Temperature");
        }
    }

}
