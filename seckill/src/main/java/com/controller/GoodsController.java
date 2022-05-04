package com.controller;

import com.pojo.User;
import com.service.IGoodsService;
import com.service.IUserService;
import com.vo.DetailVo;
import com.vo.GoodsVo;
import com.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 商品
 *
 * @author zhoubin
 * @since 1.0.0
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IUserService iUserService;
    @Autowired
    private IGoodsService iGoodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;
    /**
     * 跳转登录页
     *
     * @return
     */

    @RequestMapping(value = "/toList", produces = "text/html;charset=utf-8", method = RequestMethod.GET)
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!org.thymeleaf.util.StringUtils.isEmpty(html)) {
            return html;
        }

        model.addAttribute("user", user);
        model.addAttribute("goodsList", iGoodsService.findGoodsVo());

        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (!StringUtils.isEmpty(html)) {
            valueOperations.set("goodsList", html, 60, TimeUnit.SECONDS);
        }
        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}", method = RequestMethod.GET)
    @ResponseBody
    public RespBean toDetail(User user, @PathVariable Long goodsId) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
//        String html = (String) valueOperations.get("goodsDetail:" + goodsId);
//        if (!StringUtils.isEmpty(html)) {
//            return html;
//        }

//        model.addAttribute("user", user);
        GoodsVo goodsVo = iGoodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int seckillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            //秒杀还未开始0
            remainSeconds = (int) ((startDate.getTime() - nowDate.getTime()) / 1000);
        } else if (nowDate.after(endDate)) {
            //秒杀已经结束
            seckillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀进行中
            seckillStatus = 1;
            remainSeconds = 0;
        }
//        model.addAttribute("remainSeconds", remainSeconds);
//        model.addAttribute("goods", goodsVo);
//        model.addAttribute("seckillStatus", seckillStatus);

//        WebContext webContext = new WebContext(request, response, request.getServletContext(), request.getLocale(), model.asMap());
//        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
//        if (!StringUtils.isEmpty(html)) {
//            valueOperations.set("goodsDetail:" + goodsId, html, 60, TimeUnit.SECONDS);
//        }
        DetailVo detailVo = new DetailVo();
        detailVo.setTUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setRemainSeconds(remainSeconds);
        detailVo.setSecKillStatus(seckillStatus);
        return RespBean.success(detailVo);
    }


}