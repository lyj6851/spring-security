package cn.mrcode.imooc.springsecurity.securitybrowser;

import cn.mrcode.imooc.springsecurity.securitycore.properties.SecurityProperties;
import cn.mrcode.imooc.springsecurity.securitycore.validate.code.ValidateCodeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author : zhuqiang
 * @version : V1.0
 * @date : 2018/8/3 0:05
 */

// WebSecurityConfigurerAdapter 适配器类。专门用来做web应用的安全配置
@Configuration
public class BrowserSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;

    // 有三个configure的方法，这里使用http参数的
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 最简单的修改默认配置的方法
        // 在v5+中，该配置（表单登录）应该是默认配置了
        // basic登录（也就是弹框登录的）应该是v5-的版本默认

        ValidateCodeFilter validateCodeFilter = new ValidateCodeFilter();
        validateCodeFilter.setFailureHandler(myAuthenticationFailureHandler);
        http
                // 由源码得知，在最前面的是UsernamePasswordAuthenticationFilter
                .addFilterBefore(validateCodeFilter, UsernamePasswordAuthenticationFilter.class)
                // 定义表单登录 - 身份认证的方式
                .formLogin()
                .loginPage("/authentication/require")
                .loginProcessingUrl("/authentication/form")
                .successHandler(myAuthenticationSuccessHandler)
                .failureHandler(myAuthenticationFailureHandler)
//                .httpBasic()
                .and()
                // 对请求授权配置：注意方法名的含义，能联想到一些
                .authorizeRequests()
                // 放行这个路径
                .antMatchers("/authentication/require",
                        securityProperties.getBrowser().getLoginPage(),
                        "/code/image",  // 图形验证码接口
                        "/error"  // 图形验证码接口
                )
                .permitAll()
                .anyRequest()
                // 对任意请求都必须是已认证才能访问
                .authenticated()
                .and()
                .csrf().disable()
        ;
    }
}
