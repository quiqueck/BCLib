package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.v2.datafixer.MigrationProfile;
import org.betterx.bclib.api.v2.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchFunction<T, R> {
    R apply(T t, MigrationProfile profile) throws PatchDidiFailException;
}
