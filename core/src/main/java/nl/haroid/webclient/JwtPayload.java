package nl.haroid.webclient;

/**
 * Created by ruud on 1-12-16.
 */
public class JwtPayload {
    private String email;
    private String profileId;
    private String brand;
    private String role;

    public String getEmail() {
        return this.email;
    }

    public String getProfileId() {
        return this.profileId;
    }

    public String getBrand() {
        return this.brand;
    }

    public String getRole() {
        return this.role;
    }
}
