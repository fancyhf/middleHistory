/**
 * 词频统计实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:30:00
 * @description 词频统计数据实体，存储词汇的频率和相关性分析结果
 */
package com.historyanalysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 词频统计实体类
 * 
 * 包含词频统计的信息：
 * - 词频ID（主键）
 * - 所属分析结果
 * - 词汇内容
 * - 词汇分类
 * - 出现频率
 * - 相关性评分
 */
@Entity
@Table(name = "word_frequency")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WordFrequency {

    /**
     * 词频ID，主键，使用UUID策略生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 词频统计所属的分析结果
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @JsonIgnore
    private AnalysisResult analysisResult;

    /**
     * 词汇内容
     */
    @NotBlank(message = "词汇内容不能为空")
    @Column(name = "word", nullable = false, length = 100)
    private String word;

    /**
     * 词汇分类枚举
     */
    public enum Category {
        PERSON("person", "历史人物"),
        PLACE("place", "历史地点"),
        EVENT("event", "历史事件"),
        DYNASTY("dynasty", "朝代"),
        CONCEPT("concept", "历史概念"),
        ARTIFACT("artifact", "文物器具"),
        INSTITUTION("institution", "制度机构"),
        CULTURE("culture", "文化现象"),
        TECHNOLOGY("technology", "科技发明"),
        OTHER("other", "其他");

        private final String code;
        private final String description;

        Category(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据代码获取分类
         * 
         * @param code 分类代码
         * @return 对应的分类枚举
         */
        public static Category fromCode(String code) {
            for (Category category : values()) {
                if (category.getCode().equals(code)) {
                    return category;
                }
            }
            return OTHER;
        }
    }

    /**
     * 词汇分类
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @NotNull(message = "词汇分类不能为空")
    private Category category;

    /**
     * 出现频率
     */
    @PositiveOrZero(message = "频率不能为负数")
    @Column(name = "frequency", nullable = false)
    private Integer frequency;

    /**
     * 相关性评分（0.0-1.0）
     * 表示该词汇与历史主题的相关程度
     */
    @Column(name = "relevance_score")
    private Float relevanceScore = 0.0f;

    /**
     * 构造函数 - 创建词频统计记录
     * 
     * @param analysisResult 所属分析结果
     * @param word 词汇内容
     * @param category 词汇分类
     * @param frequency 出现频率
     */
    public WordFrequency(AnalysisResult analysisResult, String word, Category category, Integer frequency) {
        this.analysisResult = analysisResult;
        this.word = word;
        this.category = category;
        this.frequency = frequency;
    }

    /**
     * 构造函数 - 创建包含相关性评分的词频统计记录
     * 
     * @param analysisResult 所属分析结果
     * @param word 词汇内容
     * @param category 词汇分类
     * @param frequency 出现频率
     * @param relevanceScore 相关性评分
     */
    public WordFrequency(AnalysisResult analysisResult, String word, Category category, Integer frequency, Float relevanceScore) {
        this.analysisResult = analysisResult;
        this.word = word;
        this.category = category;
        this.frequency = frequency;
        this.relevanceScore = relevanceScore;
    }

    /**
     * 获取分析结果ID
     * 
     * @return 分析结果ID
     */
    public String getAnalysisId() {
        return analysisResult != null ? analysisResult.getId().toString() : null;
    }

    /**
     * 检查是否为高频词汇
     * 
     * @param threshold 频率阈值
     * @return 如果频率大于等于阈值返回true，否则返回false
     */
    public boolean isHighFrequency(int threshold) {
        return frequency >= threshold;
    }

    /**
     * 检查是否为高相关性词汇
     * 
     * @param threshold 相关性阈值
     * @return 如果相关性评分大于等于阈值返回true，否则返回false
     */
    public boolean isHighRelevance(float threshold) {
        return relevanceScore != null && relevanceScore >= threshold;
    }

    /**
     * 获取词汇重要性评分
     * 综合考虑频率和相关性
     * 
     * @return 重要性评分
     */
    public float getImportanceScore() {
        if (relevanceScore == null) {
            return frequency.floatValue();
        }
        // 重要性 = 频率 * 相关性评分
        return frequency * relevanceScore;
    }

    /**
     * 检查是否为人物类词汇
     * 
     * @return 如果是人物类返回true，否则返回false
     */
    public boolean isPerson() {
        return Category.PERSON.equals(this.category);
    }

    /**
     * 检查是否为地点类词汇
     * 
     * @return 如果是地点类返回true，否则返回false
     */
    public boolean isPlace() {
        return Category.PLACE.equals(this.category);
    }

    /**
     * 检查是否为事件类词汇
     * 
     * @return 如果是事件类返回true，否则返回false
     */
    public boolean isEvent() {
        return Category.EVENT.equals(this.category);
    }

    /**
     * 检查是否为朝代类词汇
     * 
     * @return 如果是朝代类返回true，否则返回false
     */
    public boolean isDynasty() {
        return Category.DYNASTY.equals(this.category);
    }

    /**
     * 获取格式化的相关性评分
     * 
     * @return 格式化的相关性评分字符串
     */
    public String getFormattedRelevanceScore() {
        if (relevanceScore == null) {
            return "未评分";
        }
        return String.format("%.2f", relevanceScore);
    }

    /**
     * 获取词汇的显示标签
     * 包含词汇内容和分类信息
     * 
     * @return 显示标签
     */
    public String getDisplayLabel() {
        return String.format("%s (%s)", word, category.getDescription());
    }
    
    // 手动添加getter方法
    public String getId() {
        return id;
    }
    
    public String getWord() {
        return word;
    }
    
    public Integer getFrequency() {
        return frequency;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public Float getRelevanceScore() {
        return relevanceScore;
    }
}