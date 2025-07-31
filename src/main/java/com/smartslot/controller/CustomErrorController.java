package com.smartslot.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute("javax.servlet.error.status_code");
        Object message = request.getAttribute("javax.servlet.error.message");
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            switch (statusCode) {
                case 403:
                    model.addAttribute("errorCode", "403");
                    model.addAttribute("errorTitle", "Access Denied");
                    model.addAttribute("errorMessage", "You don't have permission to access this resource.");
                    model.addAttribute("suggestion", "Please make sure you are logged in and have the required permissions.");
                    return "error";
                case 404:
                    model.addAttribute("errorCode", "404");
                    model.addAttribute("errorTitle", "Page Not Found");
                    model.addAttribute("errorMessage", "The page you're looking for doesn't exist.");
                    model.addAttribute("suggestion", "Check the URL or navigate back to the dashboard.");
                    return "error";
                case 500:
                    model.addAttribute("errorCode", "500");
                    model.addAttribute("errorTitle", "Internal Server Error");
                    model.addAttribute("errorMessage", "Something went wrong on our end.");
                    model.addAttribute("suggestion", "Please try again later or contact support.");
                    return "error";
                default:
                    model.addAttribute("errorCode", statusCode);
                    model.addAttribute("errorTitle", "Error");
                    model.addAttribute("errorMessage", message != null ? message.toString() : "An error occurred.");
                    model.addAttribute("suggestion", "Please try again or contact support.");
                    return "error";
            }
        }
        
        model.addAttribute("errorCode", "Unknown");
        model.addAttribute("errorTitle", "Error");
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        model.addAttribute("suggestion", "Please try again or contact support.");
        return "error";
    }
} 