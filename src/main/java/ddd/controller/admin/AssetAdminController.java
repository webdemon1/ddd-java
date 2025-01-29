package ddd.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ddd.model.asset.CashInOut;
import ddd.model.asset.CashInOut.FindCashInOut;
import ddd.application.admin.AssetAdminService;

/**
 * API controller of the asset domain in the organization.
 */
@RestController
@RequestMapping("/admin/asset")
@RequiredArgsConstructor
public class AssetAdminController {
    private final AssetAdminService service;

    @GetMapping("/cio")
    public List<CashInOut> findCashInOut(@Valid FindCashInOut p) {
        return service.findCashInOut(p);
    }

}
