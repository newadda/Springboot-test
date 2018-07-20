package org.onecellboy.web.security.oauth2.config;

import org.apache.logging.log4j.core.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.token.TokenService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ResourceServerConfigurerAdapter 는 Bearer Auth 이다.
 *
 * Oauth2 에 대한 인증으로 접근제어를 한다.
 *
 * WebSecurityConfigurerAdapter 와 차이점은 WebSecurityConfigurerAdapter은 basic auth이고 oauth2 사용시 인가서버에 대해서만 동작하면 된다.
 *
 * 주의점 : @EnableResourceServer 와 @EnableWebSecurity 이 같은 프로젝트에 둘다 존재한다면 @EnableWebSecurity 은 무시된다.
 *          이 말은 인증서버와 리소스 서버가 같이(같은 프로세스) 동작한다는 말이다. 이를 조심해야한다.
 *
 * 차라리 HttpSecurity 설정을 ResourceServerConfigurerAdapter 에서만 잡아버리는 것도 방법이다.
 * WebSecurityConfigurerAdapter은 없다고 치는 것이다.
 *
 *
 */
/*
 * 주의점 : @EnableResourceServer 와 @EnableWebSecurity 이 같은 프로젝트에 둘다 존재한다면 @EnableWebSecurity 은 무시된다.
 *          이 말은 인증서버와 리소스 서버가 같이(같은 프로세스) 동작한다는 말이다.
 *          이럴 때 HttpSecurity 설정은 ResourceServerConfigurerAdapter 에서 다 하는 것이 좋다.
 *          즉 oauth2 를 사용할 때는 ResourceServerConfigurerAdapter 에서 접근제어 설정
 *          Basic auth(spring security)를 사용 할 때는 WebSecurityConfigurerAdapter 에 접근제어 설정
 * */
@Configuration
@EnableResourceServer

public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    @Qualifier("tokenStore")
    private TokenStore tokenStore;


    @Autowired
    @Qualifier("localTokenServices")
    ResourceServerTokenServices tokenServices;



    /**
     * 인가서버(Authorization Server) 가 리소스 서버와 분리되어 있을 때 사용하는 방법이다.
     * 원격지에 인가서버가 존재할때..
     * @return
     */

    @Bean(name = "remoteTokenServices")
    public ResourceServerTokenServices remoteTokenServices() {
        final RemoteTokenServices tokenServices = new RemoteTokenServices();
        tokenServices.setCheckTokenEndpointUrl("http://127.0.0.1:80801/oauth/check_token"); // 인증서버 check_token 경로
        tokenServices.setClientId("my_client_id");  // client id 필수
        tokenServices.setClientSecret("my_client_secret"); // client secret 필수

        return tokenServices;
    }

    @Primary
    @Bean(name = "localTokenServices")
    public ResourceServerTokenServices localTokenServices() {
        final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        return defaultTokenServices;
    }




    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

        // ==== tokenServices 등록 (원격지 인가 서버 혹은 인가서버와 리소스서버가 동일할때)
        // 인가 서버가 원격지에 있을 때 설정이다.
        resources.tokenServices(tokenServices);

        // TODO인증 서버와 리소스 서버가 같이 동작할 때
        //resources.tokenServices(localTokenServices());

        //  ==== 만약 인증서버 한대에 서로 다른 리소스 서버가 있을 경우 Resource ID를 통해 분류가능하다.
        //resources.resourceId()


        // ===== Exception Handling ====
        // access token 실패시
        //resources.authenticationEntryPoint();

        //  access token 은 존재하나 scope, role 등 권한 위반시
        //  resources.accessDeniedHandler();
        //  ===============================



    }

    /**
     * 자원에 대한 CRUD에 대한 접근권한을 지정할 수 있다.
     * @param http
     * @throws Exception
     */

    @Override
    public void configure(HttpSecurity http) throws Exception {

        // 정의 되어 있지 않으면 기본이 허용


        // WebSecurityConfigurerAdapter 의 configure(HttpSecurity http) 코드와 동일하다.
        // 이 코드가 존재하는 이유는 리소스 서버와 인증 서버가 같은 있는 경우
        // 다시 말해 @EnableResourceServer, @EnableWebSecurity 이 같이 있는 경우 @EnableWebSecurity 은 무시된다..

        // oauth2 인증용 설정이다.
        this.oauthConfigure(http);



        // TODO 이 밑으로 접근제어 하면 된다.
        // 예시 http.authorizeRequests().antMatchers("/authtest1").access("hasRole('USER') and #oauth2.hasScope('write1')");
        /* 예시
        http.authorizeRequests().antMatchers("/").permitAll()
            .antMatchers("/my").authenticated().and().authorizeRequests().anyRequest().hasRole("USER")
            .antMatchers("/my").access("#oauth2.hasScope('read')");
        * */


        // TODO 주석을 풀면 모든 요청이 인증되어야 한다.
        //super.configure(http); // http.authorizeRequests().anyRequest().authenticated(); 모든 요청이 인증되어야 한다.




      //  http.authorizeRequests().antMatchers("/authtest2").authenticated().and().formLogin();


       // http.authorizeRequests().antMatchers("/").anonymous();
       // http.authorizeRequests().antMatchers("/authcodetest").permitAll();
       // http.authorizeRequests().antMatchers("/authtest2").denyAll();
      //  http.authorizeRequests().antMatchers("/login").permitAll();
        //http.authorizeRequests().antMatchers("/authtest2").permitAll();
     //   http.authorizeRequests().antMatchers("/login","/oauth/**").permitAll();

        //super.configure(http); // 코드를 보면 어떤 요청이든 인증을 필요로 한다.
         /* 예제
        /// 리소스 서버에서 요청에 대한 접근 권한을 미리 체크한다.
        http.authorizeRequests().antMatchers(HttpMethod.GET,"/**").access("#oauth2.hasScope('read')");
        */
         /*
        http.authorizeRequests()
                .antMatchers("/oauth/**").permitAll()
                .and().authorizeRequests().and()
                .formLogin().loginPage("/login")
                .permitAll().and().logout().permitAll().and().authorizeRequests()
                .antMatchers("/authcodetest").permitAll().antMatchers("/authtest2")
                .hasAnyRole("ADMIN").anyRequest().authenticated().and();
        */


/*
        http.authorizeRequests().antMatchers("/").permitAll()
                .antMatchers("/authcodetest").permitAll()
               .and().formLogin()
               .permitAll().and().logout().permitAll();

        http.csrf().disable();
        super.configure(http);*/


/*
        http.
                authorizeRequests()
                .antMatchers("/authtest1")
                .access("hasRole('USER') and #oauth2.hasScope('write1')");


        http.formLogin().loginProcessingUrl("/login")
                .loginPage("/loginForm")
                .permitAll();
*/
/*
        http.authorizeRequests().antMatchers("/authcodetest").permitAll()
            .antMatchers("oauth/authorize").
        ;
        http.authorizeRequests().antMatchers("/").permitAll().antMatchers("/test/test")
                .hasAnyRole("ADMIN").anyRequest().authenticated().and().formLogin()
                .permitAll().and().logout().permitAll();

        http.csrf().disable();
*/

      //  http.formLogin().permitAll();
    //   http.authorizeRequests().antMatchers("/**").permitAll();

     /*

*/
       /*
        http.authorizeRequests()
        .antMatchers("/", "/home").access("hasRole('USER') or hasRole('ADMIN') or hasRole('DBA')")
        .and().formLogin().loginPage("/login")
        .usernameParameter("ssoId").passwordParameter("password")
        .and().exceptionHandling().accessDeniedPage("/Access_Denied");
       * */
       /*
        http
                // 강의 특성상 전부 허용으로 작업하겠습니다.
                .authorizeRequests()
                .antMatchers("/**")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/sign-out") // /logout 을 호출할 경우 로그아웃
                .logoutSuccessUrl("/") 	// 로그아웃이 성공했을 경우 이동할 페이지
                .invalidateHttpSession(true)
                .and()
                .formLogin()
                .loginPage("/sign-in") // 로그인 페이지 : 컨트롤러 매핑을 하지 않으면 기본 제공되는 로그인 페이지가 뜬다.
                .loginProcessingUrl("/sign-in/auth")
                .failureUrl("/sign-in?error=exception")
                .defaultSuccessUrl("/")

                .and()
                // 여기 나오는 sso.filter 빈은 다음장에서 작성합니다.
                // 이 장에서 실행을 확인하시려면 당연히 NPE 오류가 나니 아래 소스에 주석을 걸어주시기 바랍니다.
                .addFilterBefore((Filter)context.getBean("sso.filter"), BasicAuthenticationFilter.class);
        */
    }




    /**
     * JWT 인증을 위해 존재한다.
     * @return
     */
    /*
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        final Resource resource = new ClassPathResource("public.txt");
        String publicKey = null;
        try {
            publicKey = IOUtils.toString(resource.getInputStream());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        converter.setVerifierKey(publicKey);
        return converter;
    }
*/


    public  void oauthConfigure(HttpSecurity http) throws Exception
    {
        //// /oauth/authorize 의 경우는 꼭 로그인(oauth 로그인이 아니다. 일반 사용자 로그인이다.)해야한다.
        ////  /oauth/authorize 의 response_type=code 즉 Authorization Code-Grant Type 을 보면 로그인이 왜 필요한 지 알게 될 것이다.

        http.sessionManagement().sessionFixation().changeSessionId()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and()
                .authorizeRequests()
                // 이부분이 가장 중요하다.
                // oauth2 grant type - authorization code 사용시 /oauth/authorize 요청하면 /login 으로 리다이렉션하여
                // 사용자 인증하고 다시 /oauth/authorize 다시 리다이렉션하여 code를 받게 된다.
                .antMatchers("/oauth/authorize").authenticated()
                //  .antMatchers("/login").permitAll() // login 페이지 허용, 굳이 필요없는 듯
                //.anyRequest().hasRole("USER") // 그외는 ROLE_USER 필요
                //.and()
                //.exceptionHandling()
                //.accessDeniedHandler() // 예외처리 루틴
                //.accessDeniedPage("/login.jsp?authorization_error=true") // 예외처리시 페이지
                .and()
                // TODO: put CSRF protection back into this endpoint
                .csrf()
                //.requireCsrfProtectionMatcher(new AntPathRequestMatcher("/oauth/authorize")) // CSRF 지정 패턴
                .disable() //CSRF 끄기
                .logout()
                //.logoutUrl("/logout") // 로그아웃 URL
                //.logoutSuccessUrl("/login.jsp") // 로그아웃시 리다이렉트 URL
                .and()
                //.loginProcessingUrl("/login") // POST 로 넘어오는 로그인 처리 URL
                //.loginPage("/login") //로그인 페이지, 따로 만들지 않았다면 주석 처리해야한다.
                //.failureUrl("/error") // 로그인 실패시 URL
                .formLogin() //로그인
                .permitAll();  // 허용

    }



}
