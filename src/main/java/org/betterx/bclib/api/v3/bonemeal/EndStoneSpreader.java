package org.betterx.bclib.api.v3.bonemeal;

import org.betterx.bclib.api.v3.tag.BCLBlockTags;

public class EndStoneSpreader extends TaggedBonemealBlockSpreader {
    static final EndStoneSpreader INSTANCE = new EndStoneSpreader();

    protected EndStoneSpreader() {
        super(BCLBlockTags.BONEMEAL_SOURCE_END_STONE);
    }

}
