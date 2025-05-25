package fr.uge.booqin.app.controller.mvc;

import fr.uge.booqin.app.controller.mvc.session.SessionData;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SessionData sessionData;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          SessionData sessionData,
                            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.sessionData = sessionData;
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            var securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            var user = (SecurityUser) authentication.getPrincipal();
            sessionData.setUser(user.authenticatedUser());
            sessionData.setProfile(userService.findMyProfile(user.authenticatedUser()));
            model.addAttribute("session", sessionData);
            return "redirect:/"; // Redirect to home or dashboard after login
        } catch (AuthenticationException e) {
            redirectAttributes.addAttribute("error", "Invalid usernameOrEmail or password");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}