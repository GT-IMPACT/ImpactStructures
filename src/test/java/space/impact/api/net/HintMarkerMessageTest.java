package space.impact.api.net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;
import space.impact.api.hint.HintMarkerKind;
import static org.junit.jupiter.api.Assertions.*;

class HintMarkerMessageTest {
    @Test
    void carriesDimensionCoordinatesAndEveryServiceKind() {
        for (HintMarkerKind kind : HintMarkerKind.values()) {
            ByteBuf encoded = Unpooled.buffer();
            ByteBuf restored = Unpooled.buffer();
            try {
                new HintMarkerMessage(-7, -300, 255, 120, kind).toBytes(encoded);
                assertEquals(-7, encoded.getInt(0));
                assertEquals(-300, encoded.getInt(4));
                assertEquals(255, encoded.getInt(8));
                assertEquals(120, encoded.getInt(12));
                assertEquals(kind.ordinal(), encoded.getUnsignedByte(16));
                HintMarkerMessage decoded = new HintMarkerMessage();
                decoded.fromBytes(encoded.duplicate());
                decoded.toBytes(restored);
                assertEquals(encoded, restored);
            } finally {
                encoded.release();
                restored.release();
            }
        }
    }

    @Test
    void unknownServiceKindsDoNotBecomeAValidColorOrMarker() {
        ByteBuf encoded = Unpooled.buffer();
        ByteBuf restored = Unpooled.buffer();
        try {
            encoded.writeInt(0).writeInt(0).writeInt(0).writeInt(0).writeByte(99);
            HintMarkerMessage decoded = new HintMarkerMessage();
            decoded.fromBytes(encoded);
            decoded.toBytes(restored);
            assertEquals(255, restored.getUnsignedByte(16));
        } finally {
            encoded.release();
            restored.release();
        }
    }
}
