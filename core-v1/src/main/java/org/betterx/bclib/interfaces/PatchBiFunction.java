package org.betterx.bclib.interfaces;

import org.betterx.bclib.api.v2.datafixer.MigrationProfile;
import org.betterx.bclib.api.v2.datafixer.PatchDidiFailException;

@FunctionalInterface
public interface PatchBiFunction<U, V, R> {
    R apply(U t, V v, MigrationProfile profile) throws PatchDidiFailException;
}