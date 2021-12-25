import com.mqttsnet.thinglinks.link.api.domain.product.entity.ProductTemplate;
import com.mqttsnet.thinglinks.link.service.product.impl.ProductTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
* (product_template)表控制层
*
* @author xxxxx
*/
@RestController
@RequestMapping("product_template")
public class ProductTemplateController {
/**
* 服务对象
*/
@Resource
private ProductTemplateService productTemplateService;

/**
* 通过主键查询单条数据
*
* @param id 主键
* @return 单条数据
*/
@GetMapping("selectOne")
public ProductTemplate selectOne(Integer id) {
return productTemplateService.selectByPrimaryKey(id);
}

}
