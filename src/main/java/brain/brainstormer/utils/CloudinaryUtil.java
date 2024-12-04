package brain.brainstormer.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", EnvUtil.getEnv("CLOUDINARY_CLOUD_NAME"),
                "api_key", EnvUtil.getEnv("CLOUDINARY_API_KEY"),
                "api_secret", EnvUtil.getEnv("CLOUDINARY_API_SECRET")
        ));
    }

    public static Cloudinary getInstance() {
        return cloudinary;
    }
}
