import { createAuth0 } from '@auth0/auth0-vue';

const auth0 = createAuth0({
    domain: "dev-jkopecky.us.auth0.com",
    clientId: "YVDjNf0dYIl8V3VaNyZiyJ0I0HwOe6mx",
    authorizationParams: {
        redirect_uri: window.location.origin,
        audience: "https://draftbook-auth-api"
    }
});

export default auth0;