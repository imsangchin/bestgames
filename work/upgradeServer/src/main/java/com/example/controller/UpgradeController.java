package com.example.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.service.UpgradeService;

@Controller
public class UpgradeController {

	@Autowired
	UpgradeService upgradeService;

	private static final Logger LOG = LoggerFactory
			.getLogger(UpgradeController.class);

	@RequestMapping("/v1/upgrade")
	public void auth(@RequestParam(value = "version") String version,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.getWriter().write(
				String.valueOf(upgradeService.checkUpgrade(version)));
	}
}
