/**
 * 时间轴事件实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:30:00
 * @description 时间轴事件数据实体，存储历史事件的时间和描述信息
 */
package com.historyanalysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

/**
 * 时间轴事件实体类
 * 
 * 包含时间轴事件的信息：
 * - 事件ID（主键）
 * - 所属分析结果
 * - 事件名称
 * - 事件日期
 * - 事件描述
 * - 元数据（JSON格式）
 */
@Entity
@Table(name = "timeline_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TimelineEvent {

    /**
     * 事件ID，主键，使用UUID策略生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 事件所属的分析结果
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @JsonIgnore
    private AnalysisResult analysisResult;

    /**
     * 事件名称
     */
    @NotBlank(message = "事件名称不能为空")
    @Column(name = "event_name", nullable = false, length = 200)
    private String eventName;

    /**
     * 事件发生日期
     */
    @Column(name = "event_date")
    private LocalDate eventDate;

    /**
     * 事件描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 事件元数据，以JSON格式存储
     * 包含事件的额外信息，如参与人物、地点、影响等
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata = "{}";

    /**
     * 构造函数 - 创建时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @param eventName 事件名称
     * @param eventDate 事件日期
     */
    public TimelineEvent(AnalysisResult analysisResult, String eventName, LocalDate eventDate) {
        this.analysisResult = analysisResult;
        this.eventName = eventName;
        this.eventDate = eventDate;
    }

    /**
     * 构造函数 - 创建包含描述的时间轴事件
     * 
     * @param analysisResult 所属分析结果
     * @param eventName 事件名称
     * @param eventDate 事件日期
     * @param description 事件描述
     */
    public TimelineEvent(AnalysisResult analysisResult, String eventName, LocalDate eventDate, String description) {
        this.analysisResult = analysisResult;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.description = description;
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
     * 检查事件是否有具体日期
     * 
     * @return 如果有具体日期返回true，否则返回false
     */
    public boolean hasSpecificDate() {
        return eventDate != null;
    }

    /**
     * 获取事件年份
     * 
     * @return 事件年份，如果没有日期返回null
     */
    public Integer getEventYear() {
        return eventDate != null ? eventDate.getYear() : null;
    }

    /**
     * 检查事件是否有描述
     * 
     * @return 如果有描述返回true，否则返回false
     */
    public boolean hasDescription() {
        return description != null && !description.trim().isEmpty();
    }

    /**
     * 获取事件的显示标题
     * 包含事件名称和年份信息
     * 
     * @return 显示标题
     */
    public String getDisplayTitle() {
        if (eventDate != null) {
            return String.format("%s (%d年)", eventName, eventDate.getYear());
        }
        return eventName;
    }

    /**
     * 获取格式化的事件日期
     * 
     * @return 格式化的日期字符串
     */
    public String getFormattedDate() {
        if (eventDate == null) {
            return "日期不详";
        }
        return String.format("%d年%d月%d日", eventDate.getYear(), eventDate.getMonthValue(), eventDate.getDayOfMonth());
    }

    /**
     * 获取简化的事件日期（仅年份）
     * 
     * @return 年份字符串
     */
    public String getSimplifiedDate() {
        if (eventDate == null) {
            return "年代不详";
        }
        return eventDate.getYear() + "年";
    }
    
    // 手动添加getter方法
    public String getId() {
        return id;
    }
    
    public String getEventName() {
        return eventName;
    }
    
    public LocalDate getEventDate() {
        return eventDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}