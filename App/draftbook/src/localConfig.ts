import {createAuth0} from "@auth0/auth0-vue";

export const auth0 = createAuth0({
    domain: "dev-jkopecky.us.auth0.com",
    clientId: "YVDjNf0dYIl8V3VaNyZiyJ0I0HwOe6mx",
    authorizationParams: {
        redirect_uri: window.location.origin + "/authenticate",
        audience: "https://draftbook-auth-api"
    }
});

export const API_URL = "https://draftbook.jkopecky.dev/api";
//export const API_URL = "http://localhost:443/api";