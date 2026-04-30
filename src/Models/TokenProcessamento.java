package Models;

import java.util.UUID;

public class TokenProcessamento {
    private final UUID token;

    public TokenProcessamento() {
        this.token = UUID.randomUUID();
    }

    public UUID getToken() {
        return token;
    }
}
