package hu.dysaido.fakeplayer.api.npc;

import hu.dysaido.fakeplayer.npc.NPCImpl;

import java.util.UUID;

public class AbstractNPC extends NPCImpl {

    public AbstractNPC(UUID randomUUID, String name, String skin) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        super(randomUUID, name, skin);
    }

}
