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

package app.krista.extensions.krista.authentication.oauth2_authentication;

import java.io.IOException;
import javax.inject.Inject;
import app.krista.extension.common.TokenInfo;
import app.krista.extensions.krista.authentication.oauth2_authentication.resource.ExtensionApiResource;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

@Service
public class TokenManger {

    private final KeyValueStore keyValueStore;

    @Inject
    public TokenManger(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Retrieves the stored token information for the given invoker and account.
     *
     * @param invokerId identifier of the invoker that owns the token
     * @param accountId identifier of the account for which the token was issued
     * @return {@link TokenInfo} for the account, or {@code null} if none exists
     * @throws IOException if the underlying key-value store access fails
     */
    public TokenInfo getTokenInfo(String invokerId, String accountId) throws IOException {
        System.out.println("Getting access token for account id: " + accountId);
        String key = invokerId + "_" + accountId;
        String valueString = (String) keyValueStore.get(key);
        return ExtensionApiResource.GSON_JSON_MAPPER.fromString(valueString, TokenInfo.class);
    }

    /**
     * Persists token information for the given account.
     *
     * @param tokenInfo token information to store
     * @throws IOException if the underlying key-value store access fails
     */
    public void addTokenInfo(TokenInfo tokenInfo) throws IOException {
        if (tokenInfo != null && tokenInfo.getAccountId() != null) {
            String key = tokenInfo.getInvokerId() + "_" + tokenInfo.getAccountId();
            keyValueStore.put(key, ExtensionApiResource.GSON_JSON_MAPPER.toString(tokenInfo));
        }
    }

    /**
     * Removes stored token information for the given account.
     *
     * @param invokerId identifier of the invoker that owns the token
     * @param accountId identifier of the account whose token should be removed
     * @throws IOException if the underlying key-value store access fails
     */
    public void removeTokenInfo(String invokerId, String accountId) throws IOException {
        String key = invokerId + "_" + accountId;
        keyValueStore.remove(key);
    }

}
