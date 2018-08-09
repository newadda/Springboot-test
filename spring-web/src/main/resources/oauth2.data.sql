insert into oauth_client_details (client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove) values ('my_client_id', 'my_client_secret', null, 'read,write', 'authorization_code,password,client_credentials,implicit,refresh_token', 'http://localhost:8080/,http://localhost:8080/callback', 'ROLE_MY_CLIENT', 36000, 2592000, null, 'true');

insert into oauth_client_details (client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove, logout_uri, base_uri) values ('System1_id', 'System1_secret', null, 'read', 'authorization_code', 'http://localhost:18010/oauthCallback', 'ROLE_YOUR_CLIENT', 36000, 2592000, null, 'true', 'http://localhost:18010/logout', 'http://localhost:18010/me');

insert into oauth_client_details (client_id, client_secret, resource_ids, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove, logout_uri, base_uri) values ('System7_id', 'System7_secret', null, 'read', 'authorization_code', 'http://localhost:18070/oauthCallback', 'ROLE_YOUR_CLIENT', 36000, 2592000, null, 'true', 'http://localhost:18070/logout', 'http://localhost:18070/me');
