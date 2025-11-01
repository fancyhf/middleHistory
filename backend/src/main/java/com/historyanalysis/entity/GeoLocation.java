/**
 * 地理位置实体类
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 15:30:00
 * @description 地理位置数据实体，存储历史地点的坐标和相关信息
 */
package com.historyanalysis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 地理位置实体类
 * 
 * 包含地理位置的信息：
 * - 位置ID（主键）
 * - 所属分析结果
 * - 位置名称
 * - 纬度和经度
 * - 位置类型
 * - 元数据（JSON格式）
 */
@Entity
@Table(name = "geo_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GeoLocation {

    /**
     * 位置ID，主键，使用UUID策略生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    /**
     * 地理位置所属的分析结果
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    @JsonIgnore
    private AnalysisResult analysisResult;

    /**
     * 位置名称
     */
    @NotBlank(message = "位置名称不能为空")
    @Column(name = "location_name", nullable = false, length = 200)
    private String locationName;

    /**
     * 纬度
     */
    @Column(name = "latitude")
    private Float latitude;

    /**
     * 经度
     */
    @Column(name = "longitude")
    private Float longitude;

    /**
     * 位置类型枚举
     */
    public enum LocationType {
        CITY("city", "城市"),
        PROVINCE("province", "省份"),
        COUNTRY("country", "国家"),
        MOUNTAIN("mountain", "山脉"),
        RIVER("river", "河流"),
        LAKE("lake", "湖泊"),
        BATTLEFIELD("battlefield", "战场"),
        PALACE("palace", "宫殿"),
        TEMPLE("temple", "寺庙"),
        TOMB("tomb", "陵墓"),
        BORDER("border", "边界"),
        TRADE_ROUTE("trade_route", "贸易路线"),
        OTHER("other", "其他");

        private final String code;
        private final String description;

        LocationType(String code, String description) {
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
         * 根据代码获取位置类型
         * 
         * @param code 位置类型代码
         * @return 对应的位置类型枚举
         */
        public static LocationType fromCode(String code) {
            for (LocationType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return OTHER;
        }
    }

    /**
     * 位置类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", length = 50)
    private LocationType locationType = LocationType.OTHER;

    /**
     * 位置元数据，以JSON格式存储
     * 包含位置的额外信息，如历史意义、相关事件等
     */
    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata = "{}";

    /**
     * 构造函数 - 创建地理位置
     * 
     * @param analysisResult 所属分析结果
     * @param locationName 位置名称
     * @param latitude 纬度
     * @param longitude 经度
     */
    public GeoLocation(AnalysisResult analysisResult, String locationName, Float latitude, Float longitude) {
        this.analysisResult = analysisResult;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * 构造函数 - 创建包含类型的地理位置
     * 
     * @param analysisResult 所属分析结果
     * @param locationName 位置名称
     * @param latitude 纬度
     * @param longitude 经度
     * @param locationType 位置类型
     */
    public GeoLocation(AnalysisResult analysisResult, String locationName, Float latitude, Float longitude, LocationType locationType) {
        this.analysisResult = analysisResult;
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationType = locationType;
    }

    /**
     * 获取分析结果ID
     * 
     * @return 分析结果ID
     */
    public String getAnalysisId() {
        return analysisResult != null ? String.valueOf(analysisResult.getId()) : null;
    }

    /**
     * 检查是否有坐标信息
     * 
     * @return 如果有坐标信息返回true，否则返回false
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /**
     * 检查是否为城市类型
     * 
     * @return 如果是城市类型返回true，否则返回false
     */
    public boolean isCity() {
        return LocationType.CITY.equals(this.locationType);
    }

    /**
     * 检查是否为省份类型
     * 
     * @return 如果是省份类型返回true，否则返回false
     */
    public boolean isProvince() {
        return LocationType.PROVINCE.equals(this.locationType);
    }

    /**
     * 检查是否为国家类型
     * 
     * @return 如果是国家类型返回true，否则返回false
     */
    public boolean isCountry() {
        return LocationType.COUNTRY.equals(this.locationType);
    }

    /**
     * 检查是否为自然地理类型（山脉、河流、湖泊）
     * 
     * @return 如果是自然地理类型返回true，否则返回false
     */
    public boolean isNaturalFeature() {
        return LocationType.MOUNTAIN.equals(this.locationType) ||
               LocationType.RIVER.equals(this.locationType) ||
               LocationType.LAKE.equals(this.locationType);
    }

    /**
     * 检查是否为历史建筑类型（宫殿、寺庙、陵墓）
     * 
     * @return 如果是历史建筑类型返回true，否则返回false
     */
    public boolean isHistoricalBuilding() {
        return LocationType.PALACE.equals(this.locationType) ||
               LocationType.TEMPLE.equals(this.locationType) ||
               LocationType.TOMB.equals(this.locationType);
    }

    /**
     * 获取格式化的坐标字符串
     * 
     * @return 格式化的坐标字符串
     */
    public String getFormattedCoordinates() {
        if (!hasCoordinates()) {
            return "坐标未知";
        }
        return String.format("%.6f, %.6f", latitude, longitude);
    }

    /**
     * 获取位置的显示标签
     * 包含位置名称和类型信息
     * 
     * @return 显示标签
     */
    public String getDisplayLabel() {
        return String.format("%s (%s)", locationName, locationType.getDescription());
    }

    /**
     * 计算与另一个地理位置的距离（简化计算）
     * 
     * @param other 另一个地理位置
     * @return 距离（公里），如果任一位置缺少坐标返回null
     */
    public Double calculateDistance(GeoLocation other) {
        if (!this.hasCoordinates() || !other.hasCoordinates()) {
            return null;
        }

        // 使用简化的球面距离公式
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double deltaLatRad = Math.toRadians(other.latitude - this.latitude);
        double deltaLonRad = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 地球半径（公里）
        double earthRadius = 6371.0;
        return earthRadius * c;
    }
    
    // 手动添加getter方法
    public String getId() {
        return id;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public LocationType getLocationType() {
        return locationType;
    }
    
    public Float getLatitude() {
        return latitude;
    }
    
    public Float getLongitude() {
        return longitude;
    }
    
    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}