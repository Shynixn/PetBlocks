package com.github.shynixn.petblocks.api.entities;

import com.github.shynixn.petblocks.business.Config;

/**
 * Created by Shynixn
 */
@Deprecated
public enum Age {
    SMALL,
    LARGE;

    public int getTicks() {
        return Config.getInstance().getTicksFromAge(this);
    }

    public static Age getAgeFromTicks(int ticks) {
        Age last = null;
        for (final Age age : Age.values()) {
            if (ticks == age.getTicks()) {
                return age;
            }
            if (ticks > age.getTicks()) {
                last = age;
            } else {
                return last;
            }
        }
        return last;
    }
}
