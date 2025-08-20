package com.imooc.mall.service.impl;

import com.imooc.mall.consts.MallConst;
import com.imooc.mall.dao.CategoryMapper;
import com.imooc.mall.pojo.Category;
import com.imooc.mall.service.ICategoryService;
import com.imooc.mall.vo.CategoryVo;
import com.imooc.mall.vo.ResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/23 14:07
 */
@Service
@Slf4j
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ResponseVo<List<CategoryVo>> selectAll() {
        List<CategoryVo> categoryVoList = new ArrayList<>();
        //查出所有类别
        List<Category> categories = categoryMapper.selectAll();
        //查出parent_id=0,查询父目录           一级目录：categoryVoList
        for (Category category : categories) {
            if (category.getParentId().equals(MallConst.ROOT_PARENT_ID)) {
                CategoryVo categoryVo = new CategoryVo();
                BeanUtils.copyProperties(category, categoryVo);//拷贝对象:BeanUtils.copyProperties(原对象，对象)
                categoryVoList.add(categoryVo);
                categoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());//对父目录按照sortOrder进行排序，数字越大排前面
            }
        }
        //查询子目录
        findSubCategory(categoryVoList, categories);
        return ResponseVo.success(categoryVoList);
    }

    @Override
    public void findSubCategoryId(Integer id, Set<Integer> hashset) {
        List<Category> categories = categoryMapper.selectAll();
//        for (Category category : categories){
//            if(category.getParentId().equals(id)){
//                hashset.add(category.getId());
//                findSubCategoryId(category.getId(),hashset);
//            }
//        }
        findSubCategoryId(id,hashset,categories);
    }
    //重写了findSubCategoryId方法，避免了每次递归调用都会查询一遍数据库，节省了时间。
    public void findSubCategoryId(Integer id, Set<Integer> hashSet,List<Category> categories){
        for (Category category : categories){
            if(category.getParentId().equals(id)){
                hashSet.add(category.getId());
                findSubCategoryId(category.getId(),hashSet,categories);
            }
        }
    }

    private void findSubCategory(List<CategoryVo> categoryVoList, List<Category> categories) {
        //categoryVoList-->父类别list        categories-->全部类别
        for (CategoryVo categoryVo : categoryVoList) {
            List<CategoryVo> subCategoryVoList = new ArrayList<>();//设置子目录
            for (Category category : categories) {
                //如果查到，设置subCategory,继续往下查
                if (categoryVo.getId().equals(category.getParentId())) {
                    CategoryVo categoryVo1 = new CategoryVo();
                    BeanUtils.copyProperties(category, categoryVo1);
                    subCategoryVoList.add(categoryVo1);
                }
                //对子目录按照sortOrder进行排序，数字越大排前面
                subCategoryVoList.sort(Comparator.comparing(CategoryVo::getSortOrder).reversed());
                categoryVo.setSubCategories(subCategoryVoList);
                findSubCategory(subCategoryVoList, categories);//继续查子目录的子目录
            }
        }
    }
}
