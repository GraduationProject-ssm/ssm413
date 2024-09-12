package com.controller;


import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.StringUtil;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;

import com.entity.WeixiuyuyueEntity;

import com.service.WeixiuyuyueService;
import com.entity.view.WeixiuyuyueView;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.service.YuangongService;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 维修预约
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/weixiuyuyue")
public class WeixiuyuyueController {
    private static final Logger logger = LoggerFactory.getLogger(WeixiuyuyueController.class);

    @Autowired
    private WeixiuyuyueService weixiuyuyueService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;



    //级联表service
    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private YuangongService yuangongService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role)){
            return R.error(511,"权限为空");
        }
        else if("用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        else if("员工".equals(role)){
            params.put("yuangongId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = weixiuyuyueService.queryPage(params);

        //字典表数据转换
        List<WeixiuyuyueView> list =(List<WeixiuyuyueView>)page.getList();
        for(WeixiuyuyueView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WeixiuyuyueEntity weixiuyuyue = weixiuyuyueService.selectById(id);
        if(weixiuyuyue !=null){
            //entity转view
            WeixiuyuyueView view = new WeixiuyuyueView();
            BeanUtils.copyProperties( weixiuyuyue , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(weixiuyuyue.getYonghuId());
            if(yonghu != null){
                BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                view.setYonghuId(yonghu.getId());
            }
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody WeixiuyuyueEntity weixiuyuyue, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,weixiuyuyue:{}",this.getClass().getName(),weixiuyuyue.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role)){
            return R.error(511,"权限为空");
        }
        else if("用户".equals(role)){
            weixiuyuyue.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        }
        Wrapper<WeixiuyuyueEntity> queryWrapper = new EntityWrapper<WeixiuyuyueEntity>()
            .eq("weixiuyuyue_name", weixiuyuyue.getWeixiuyuyueName())
            .eq("yonghu_id", weixiuyuyue.getYonghuId())
            .eq("weixiuyuyue_chexing", weixiuyuyue.getWeixiuyuyueChexing())
            .eq("weixiuyuyue_types", weixiuyuyue.getWeixiuyuyueTypes())
            .eq("shifoudaoda_types", weixiuyuyue.getShifoudaodaTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeixiuyuyueEntity weixiuyuyueEntity = weixiuyuyueService.selectOne(queryWrapper);
        if(weixiuyuyueEntity==null){
            weixiuyuyue.setCreateTime(new Date());
            weixiuyuyueService.insert(weixiuyuyue);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody WeixiuyuyueEntity weixiuyuyue, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,weixiuyuyue:{}",this.getClass().getName(),weixiuyuyue.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role)){
            return R.error(511,"权限为空");
        }
        else if("用户".equals(role)){
            weixiuyuyue.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        }
        //根据字段查询是否有相同数据
        Wrapper<WeixiuyuyueEntity> queryWrapper = new EntityWrapper<WeixiuyuyueEntity>()
            .notIn("id",weixiuyuyue.getId())
            .andNew()
            .eq("weixiuyuyue_name", weixiuyuyue.getWeixiuyuyueName())
            .eq("yonghu_id", weixiuyuyue.getYonghuId())
            .eq("weixiuyuyue_chexing", weixiuyuyue.getWeixiuyuyueChexing())
            .eq("weixiuyuyue_types", weixiuyuyue.getWeixiuyuyueTypes())
            .eq("shifoudaoda_types", weixiuyuyue.getShifoudaodaTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeixiuyuyueEntity weixiuyuyueEntity = weixiuyuyueService.selectOne(queryWrapper);
        if(weixiuyuyueEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      weixiuyuyue.set
            //  }
            weixiuyuyueService.updateById(weixiuyuyue);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        weixiuyuyueService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }



    /**
    * 前端列表
    */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isEmpty(role)){
            return R.error(511,"权限为空");
        }
        else if("用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        else if("员工".equals(role)){
            params.put("yuangongId",request.getSession().getAttribute("userId"));
        }
        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = weixiuyuyueService.queryPage(params);

        //字典表数据转换
        List<WeixiuyuyueView> list =(List<WeixiuyuyueView>)page.getList();
        for(WeixiuyuyueView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c);
        }
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        WeixiuyuyueEntity weixiuyuyue = weixiuyuyueService.selectById(id);
            if(weixiuyuyue !=null){
                //entity转view
                WeixiuyuyueView view = new WeixiuyuyueView();
                BeanUtils.copyProperties( weixiuyuyue , view );//把实体数据重构到view中

                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(weixiuyuyue.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody WeixiuyuyueEntity weixiuyuyue, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,weixiuyuyue:{}",this.getClass().getName(),weixiuyuyue.toString());
        Wrapper<WeixiuyuyueEntity> queryWrapper = new EntityWrapper<WeixiuyuyueEntity>()
            .eq("weixiuyuyue_name", weixiuyuyue.getWeixiuyuyueName())
            .eq("yonghu_id", weixiuyuyue.getYonghuId())
            .eq("weixiuyuyue_chexing", weixiuyuyue.getWeixiuyuyueChexing())
            .eq("weixiuyuyue_types", weixiuyuyue.getWeixiuyuyueTypes())
            .eq("shifoudaoda_types", weixiuyuyue.getShifoudaodaTypes())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        WeixiuyuyueEntity weixiuyuyueEntity = weixiuyuyueService.selectOne(queryWrapper);
        if(weixiuyuyueEntity==null){
            weixiuyuyue.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      weixiuyuyue.set
        //  }
        weixiuyuyueService.insert(weixiuyuyue);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }





}

