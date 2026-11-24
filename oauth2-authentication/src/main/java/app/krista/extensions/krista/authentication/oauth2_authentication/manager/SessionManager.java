/*
 * Oauth2 Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.krista.authentication.oauth2_authentication.manager;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

@Service
public class SessionManager {

    private final KeyValueStore keyValueStore;

    @Inject
    public SessionManager(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Creates a new session for the given account and persists it in the key-value store.
     *
     * @param accountId identifier of the authenticated account
     * @return generated unique session id
     */
    public String createSession(String accountId) {
        String sessionId = UUID.randomUUID().toString();
        keyValueStore.put(toKey(sessionId), accountId);
        return sessionId;
    }

    /**
     * Resolves the account identifier associated with the given session id.
     *
     * @param sessionId session identifier previously issued by {@link #createSession(String)}
     * @return account id stored for the session
     * @throws IOException if the underlying key-value store access fails
     */
    public String getAccountId(String sessionId) throws IOException {
        return keyValueStore.get(toKey(sessionId), String.class);
    }

    /**
     * Deletes the session associated with the given session id.
     *
     * @param sessionId session identifier to remove
     * @throws IOException if the underlying key-value store access fails
     */
    public void deleteSession(String sessionId) throws IOException {
        keyValueStore.remove(toKey(sessionId));
    }

    /**
     * Builds the storage key for a given session id.
     *
     * @param sessionId raw session identifier
     * @return key under which the session is stored in the key-value store
     */
    private String toKey(String sessionId) {
        Objects.requireNonNull(sessionId);
        return "session-" + sessionId;
    }

}
