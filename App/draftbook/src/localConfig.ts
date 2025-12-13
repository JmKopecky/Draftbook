import {createAuth0} from "@auth0/auth0-vue";
import {isPlatform} from "@ionic/vue";

//mobile redirect uri
const redirectUri = 'dev.jkopecky.draftbook://dev-jkopecky.us.auth0.com/capacitor/dev.jkopecky.draftbook/callback';

//web redirect uri
//const redirectUri = window.location.origin + "/authenticate";

export const auth0 = createAuth0({
    domain: "dev-jkopecky.us.auth0.com",
    clientId: "YVDjNf0dYIl8V3VaNyZiyJ0I0HwOe6mx",
    useRefreshTokens: true,
    useRefreshTokensFallback: false,
    cacheLocation: 'localstorage',
    authorizationParams: {
        redirect_uri: redirectUri,
        audience: "https://draftbook-auth-api"
    }
});

export const API_URL = "https://draftbook.jkopecky.dev/api";
//export const API_URL = "http://localhost:443/api";