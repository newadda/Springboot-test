package org.onecell.spring.template.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.catalina.Context;
import org.apache.catalina.ThreadBindingListener;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.AbstractResourceBasedMessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;


import javax.servlet.ServletException;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Configuration
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = {"org.waterworks.web"})
public class MvcConfig implements WebMvcConfigurer {
    public static final String DEFAULT_ENCODING= "UTF-8";

    /*
    @Bean
    public TomcatServletWebServerFactory servletContainerFactory() {
        return new TomcatServletWebServerFactory() {

            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                // webapps directory does not exist by default, needs to be created
                File catalinaBase = tomcat.getServer().getCatalinaBase();
                File webapps = new File(catalinaBase, "webapps");
                webapps.mkdirs();


                // Add a war with given context path
                // Can add multiple wars this way with different context paths
                Context context = tomcat.addWebapp("/geoserver", "C:\\Users\\shh\\Documents\\Shin\\workspace\\git\\MulBangUl_Server\\module-app-server\\libs\\geoserver.war");

                return super.getTomcatWebServer(tomcat);
            }

        };
    }*/







    /**
     * Spring Default validator를 설정, Spring 에서 기본적으로 사용하는 Validator를 바꾸기 위해서 오버라이드한다.
     * @return
     */
    @Bean
    @Override
    public Validator getValidator() {
        return validator();
    }

    /**
     * Make sure dates are serialised in
     * ISO-8601 format instead as timestamps
     * Date 포맷을 ISO 8601 포맷으로 직렬화, 역직렬화 하기 위해서 사용
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonMessageConverter  = (MappingJackson2HttpMessageConverter) converter;
                ObjectMapper objectMapper  = jsonMessageConverter.getObjectMapper();
                objectMapper.setTimeZone(TimeZone.getDefault());
                objectMapper.setLocale(Locale.getDefault());

                objectMapper.disable(
                        SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
                );
                objectMapper.disable(
                        SerializationFeature.FAIL_ON_EMPTY_BEANS
                );
                break;
            }
        }

    }


    /**
     * 빌트인 요청 로깅이다.
     * @return
     */
    @Bean
    public CommonsRequestLoggingFilter logFilter() throws ServletException {
        CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeClientInfo(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setBeforeMessagePrefix("REQUEST : ");
        filter.setAfterMessagePrefix("RESPONSE : ");
        filter.afterPropertiesSet();
        return filter;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
       // registry.addResourceHandler("/*.js/**").addResourceLocations("/ui/static/");
       // registry.addResourceHandler("/*.css/**").addResourceLocations("/ui/static/");
    }


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //registry.addViewController("/").setViewName("login");
        //registry.addViewController("/login").setViewName("login");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {


    }


    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/jsp/");
        resolver.setSuffix(".jsp");
      //  resolver.setViewClass (JstlView.class);
       // resolver.setViewNames("*");
        return resolver;
    }

    @Bean
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
        validatorFactoryBean.setValidationMessageSource(messageSource());
        validatorFactoryBean.afterPropertiesSet();
        return validatorFactoryBean;
    }

    /**
     * "classpath:/org/hibernate/validator/ValidationMessages" 은 하이퍼네이트 validationMessages 여서 추가했다.
     * @return
     */
    @Bean
    public AbstractResourceBasedMessageSource messageSource(){

        ReloadableResourceBundleMessageSource bundle = new ReloadableResourceBundleMessageSource();
        // classpath:/ValidationMessages : 기본 , classpath:/org/onecellboy/common/spring/ValidationMessages : custom
        bundle.setBasenames("classpath:ValidationMessages","classpath:/org/one/lib/ValidationMessages","classpath:/org/waterworks/lib/ValidationMessages"
                ,"classpath:/org/waterworks/lib/Messages","classpath:/org/hibernate/validator/ValidationMessages");
     //   bundle.setUseCodeAsDefaultMessage(true);

        bundle.setFallbackToSystemLocale(true);
        bundle.setDefaultEncoding("UTF-8");

        return bundle;
    }


    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    @Bean
    public LocaleResolver localeResolver(){
        LocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        ((AcceptHeaderLocaleResolver) localeResolver).setDefaultLocale(Locale.KOREA);
        return  localeResolver;
    }

    /**
     * Multipart Resolver Config
     * @return
     */
    @Bean
    public CommonsMultipartResolver multipartResolver()
    {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        /* 최대 업로드 가능한 바이트 크기, -1 제한없음, 기본 값 -1*/
        commonsMultipartResolver.setMaxUploadSize(-1);
        /* 디스크에 임시 파일을 생성하기 전에 메모리에 보관할 수 있는 최대 바이트 크기, 기본값은 10240*/
        commonsMultipartResolver.setMaxInMemorySize(10240);
        /* 파싱할 때 사용할 캐릭터 인코딩, 지정하지 않은 경우 httpServletRequest.setEncording()메서드로 지정한 캐릭터 셋 사용, 아무 값도 없을 경우 ISO-//59-1 사용 */
       // commonsMultipartResolver.setDefaultEncoding();

        return commonsMultipartResolver;
    }




}