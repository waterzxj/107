package room107.service.api.weixin.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

/**
 * @author WangXiao
 */
@NoArgsConstructor
public class ScanEventRequest extends AbstractEventRequest {

    /**
     * The scene value in the event key.
     * <ul>
     * <li>{@value #UNKNOWN_ID}: other</li>
     * <li>1: search house</li>
     * <li>2: weixin introduction</li>
     * <li>>{@value #STATIC_MAX_ID}: dynamic</li>
     * </ul>
     * 
     * @author WangXiao
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @ToString
    public static class Scene {

        @Getter
        @NonNull
        private int value;

        private static final int UNKNOWN_ID = 0;

        private static final int STATIC_MAX_ID = 100000;

        private static final String EVENT_KEY_PREFIX = "qrscene_";

        /**
         * userId -> sceneValue
         */
        public static int sceneValue(long userId) {
            return STATIC_MAX_ID + (int) userId;
        }

        public Scene(String scene) {
            if (StringUtils.isEmpty(scene)) {
                value = UNKNOWN_ID;
                return;
            }
            try {
                // subscribe
                value = Integer.parseInt(scene);
            } catch (Exception e) {
                // bind
                value = Integer.parseInt(scene.substring(EVENT_KEY_PREFIX
                        .length()));
            }
        }

        /**
         * @throws SceneException
         *             when scene value doesn't donate an user ID
         */
        public long getUserId() throws SceneException {
            long result = value - STATIC_MAX_ID;
            if (result <= 0) {
                throw new SceneException("NOT userId: " + value);
            }
            return result;
        }

    }

    public static class SceneException extends Exception {
        private static final long serialVersionUID = -3517502156030324973L;

        public SceneException(String message) {
            super(message);
        }
    }

    @Getter
    protected Scene scene;

    public ScanEventRequest(Element root) {
        super(root);
        scene = new Scene(getNonEmptyValue(root, EVENT_KEY));
    }

}
