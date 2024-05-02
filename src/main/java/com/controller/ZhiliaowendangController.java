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

import com.entity.ZhiliaowendangEntity;

import com.service.ZhiliaowendangService;
import com.entity.view.ZhiliaowendangView;
import com.service.YonghuService;
import com.entity.YonghuEntity;
import com.utils.PageUtils;
import com.utils.R;

/**
 * 面试通知
 * 后端接口
 * @author
 * @email
 * @date 2021-04-06
*/
@RestController
@Controller
@RequestMapping("/zhiliaowendang")
public class ZhiliaowendangController {
    private static final Logger logger = LoggerFactory.getLogger(ZhiliaowendangController.class);

    @Autowired
    private ZhiliaowendangService zhiliaowendangService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;


    //级联表service
    @Autowired
    private YonghuService yonghuService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(StringUtil.isNotEmpty(role) && "用户".equals(role)){
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        }
        params.put("orderBy","id");
        PageUtils page = zhiliaowendangService.queryPage(params);

        //字典表数据转换
        List<ZhiliaowendangView> list =(List<ZhiliaowendangView>)page.getList();
        for(ZhiliaowendangView c:list){
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
        ZhiliaowendangEntity zhiliaowendang = zhiliaowendangService.selectById(id);
        if(zhiliaowendang !=null){
            //entity转view
            ZhiliaowendangView view = new ZhiliaowendangView();
            BeanUtils.copyProperties( zhiliaowendang , view );//把实体数据重构到view中

            //级联表
            YonghuEntity yonghu = yonghuService.selectById(zhiliaowendang.getYonghuId());
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
    public R save(@RequestBody ZhiliaowendangEntity zhiliaowendang, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,zhiliaowendang:{}",this.getClass().getName(),zhiliaowendang.toString());
        Wrapper<ZhiliaowendangEntity> queryWrapper = new EntityWrapper<ZhiliaowendangEntity>()
            .eq("zhiliaowendang_name", zhiliaowendang.getZhiliaowendangName())
            .eq("zhiliaowendang_daxiao", zhiliaowendang.getZhiliaowendangDaxiao())
            .eq("yonghu_id", zhiliaowendang.getYonghuId())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhiliaowendangEntity zhiliaowendangEntity = zhiliaowendangService.selectOne(queryWrapper);
        if(zhiliaowendangEntity==null){
            zhiliaowendang.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      zhiliaowendang.set
        //  }
            zhiliaowendangService.insert(zhiliaowendang);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ZhiliaowendangEntity zhiliaowendang, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,zhiliaowendang:{}",this.getClass().getName(),zhiliaowendang.toString());
        //根据字段查询是否有相同数据
        Wrapper<ZhiliaowendangEntity> queryWrapper = new EntityWrapper<ZhiliaowendangEntity>()
            .notIn("id",zhiliaowendang.getId())
            .andNew()
            .eq("zhiliaowendang_name", zhiliaowendang.getZhiliaowendangName())
            .eq("zhiliaowendang_daxiao", zhiliaowendang.getZhiliaowendangDaxiao())
            .eq("yonghu_id", zhiliaowendang.getYonghuId())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ZhiliaowendangEntity zhiliaowendangEntity = zhiliaowendangService.selectOne(queryWrapper);
        if("".equals(zhiliaowendang.getZhiliaowendangPhoto()) || "null".equals(zhiliaowendang.getZhiliaowendangPhoto())){
                zhiliaowendang.setZhiliaowendangPhoto(null);
        }
        if("".equals(zhiliaowendang.getZhiliaowendangFile()) || "null".equals(zhiliaowendang.getZhiliaowendangFile())){
                zhiliaowendang.setZhiliaowendangFile(null);
        }
        if(zhiliaowendangEntity==null){
            //  String role = String.valueOf(request.getSession().getAttribute("role"));
            //  if("".equals(role)){
            //      zhiliaowendang.set
            //  }
            zhiliaowendangService.updateById(zhiliaowendang);//根据id更新
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
        zhiliaowendangService.deleteBatchIds(Arrays.asList(ids));
        return R.ok();
    }



    /**
    * 前端列表
    */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = zhiliaowendangService.queryPage(params);

        //字典表数据转换
        List<ZhiliaowendangView> list =(List<ZhiliaowendangView>)page.getList();
        for(ZhiliaowendangView c:list){
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
    ZhiliaowendangEntity zhiliaowendang = zhiliaowendangService.selectById(id);
        if(zhiliaowendang !=null){
            //entity转view
    ZhiliaowendangView view = new ZhiliaowendangView();
            BeanUtils.copyProperties( zhiliaowendang , view );//把实体数据重构到view中

            //级联表
                YonghuEntity yonghu = yonghuService.selectById(zhiliaowendang.getYonghuId());
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
    public R add(@RequestBody ZhiliaowendangEntity zhiliaowendang, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,zhiliaowendang:{}",this.getClass().getName(),zhiliaowendang.toString());
        Wrapper<ZhiliaowendangEntity> queryWrapper = new EntityWrapper<ZhiliaowendangEntity>()
            .eq("zhiliaowendang_name", zhiliaowendang.getZhiliaowendangName())
            .eq("zhiliaowendang_daxiao", zhiliaowendang.getZhiliaowendangDaxiao())
            .eq("yonghu_id", zhiliaowendang.getYonghuId())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
    ZhiliaowendangEntity zhiliaowendangEntity = zhiliaowendangService.selectOne(queryWrapper);
        if(zhiliaowendangEntity==null){
            zhiliaowendang.setCreateTime(new Date());
        //  String role = String.valueOf(request.getSession().getAttribute("role"));
        //  if("".equals(role)){
        //      zhiliaowendang.set
        //  }
        zhiliaowendangService.insert(zhiliaowendang);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}

