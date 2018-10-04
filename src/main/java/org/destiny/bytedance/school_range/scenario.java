package org.destiny.bytedance.school_range;

/**
 * 观点类接口
 * 由各个学校去实现, 用来描述每个场景是否正确
 */
interface scenario {

    boolean judge(String range);
}
