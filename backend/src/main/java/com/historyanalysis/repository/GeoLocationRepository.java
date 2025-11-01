/**
 * 地理位置数据访问接口
 * @author AI Agent
 * @version 1.0.0
 * @created 2024-12-29 16:05:00
 * @description 地理位置实体的数据访问层接口，提供地理位置相关的数据库操作
 */
package com.historyanalysis.repository;

import com.historyanalysis.entity.AnalysisResult;
import com.historyanalysis.entity.GeoLocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地理位置数据访问接口
 * 
 * 提供地理位置相关的数据库操作：
 * - 基础CRUD操作（继承自JpaRepository）
 * - 根据分析结果查询地理位置
 * - 根据位置类型查询地理位置
 * - 地理坐标查询
 * - 地理位置统计查询
 */
@Repository
public interface GeoLocationRepository extends JpaRepository<GeoLocation, String> {

    /**
     * 根据分析结果查找地理位置列表
     * 
     * @param analysisResult 所属分析结果
     * @return 分析结果的地理位置列表
     */
    List<GeoLocation> findByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找地理位置列表
     * 
     * @param analysisId 分析结果ID
     * @return 分析结果的地理位置列表
     */
    List<GeoLocation> findByAnalysisResultId(Long analysisId);

    /**
     * 根据位置类型查找地理位置列表
     * 
     * @param locationType 位置类型
     * @return 指定类型的地理位置列表
     */
    List<GeoLocation> findByLocationType(GeoLocation.LocationType locationType);

    /**
     * 根据分析结果和位置类型查找地理位置列表
     * 
     * @param analysisResult 所属分析结果
     * @param locationType 位置类型
     * @return 地理位置列表
     */
    List<GeoLocation> findByAnalysisResultAndLocationType(AnalysisResult analysisResult, GeoLocation.LocationType locationType);

    /**
     * 根据分析结果ID和位置类型查找地理位置列表
     * 
     * @param analysisId 分析结果ID
     * @param locationType 位置类型
     * @return 地理位置列表
     */
    List<GeoLocation> findByAnalysisResultIdAndLocationType(Long analysisId, GeoLocation.LocationType locationType);

    /**
     * 根据位置名称模糊查询地理位置
     * 
     * @param locationName 位置名称关键字
     * @return 匹配的地理位置列表
     */
    List<GeoLocation> findByLocationNameContainingIgnoreCase(String locationName);

    /**
     * 查找有坐标信息的地理位置
     * 
     * @return 有坐标信息的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<GeoLocation> findLocationsWithCoordinates();

    /**
     * 查找没有坐标信息的地理位置
     * 
     * @return 没有坐标信息的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.latitude IS NULL OR g.longitude IS NULL")
    List<GeoLocation> findLocationsWithoutCoordinates();

    /**
     * 根据分析结果查找有坐标信息的地理位置
     * 
     * @param analysisResult 所属分析结果
     * @return 有坐标信息的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult = :analysisResult AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<GeoLocation> findLocationsWithCoordinatesByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找有坐标信息的地理位置
     * 
     * @param analysisId 分析结果ID
     * @return 有坐标信息的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<GeoLocation> findLocationsWithCoordinatesByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 根据坐标范围查找地理位置
     * 
     * @param minLatitude 最小纬度
     * @param maxLatitude 最大纬度
     * @param minLongitude 最小经度
     * @param maxLongitude 最大经度
     * @return 指定坐标范围内的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.latitude BETWEEN :minLatitude AND :maxLatitude AND g.longitude BETWEEN :minLongitude AND :maxLongitude")
    List<GeoLocation> findLocationsByCoordinateRange(@Param("minLatitude") Float minLatitude, 
                                                    @Param("maxLatitude") Float maxLatitude, 
                                                    @Param("minLongitude") Float minLongitude, 
                                                    @Param("maxLongitude") Float maxLongitude);

    /**
     * 根据中心点和半径查找附近的地理位置（简化计算）
     * 
     * @param centerLatitude 中心点纬度
     * @param centerLongitude 中心点经度
     * @param radiusDegrees 半径（度数）
     * @return 附近的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.latitude IS NOT NULL AND g.longitude IS NOT NULL " +
           "AND ABS(g.latitude - :centerLatitude) <= :radiusDegrees " +
           "AND ABS(g.longitude - :centerLongitude) <= :radiusDegrees")
    List<GeoLocation> findNearbyLocations(@Param("centerLatitude") Float centerLatitude, 
                                         @Param("centerLongitude") Float centerLongitude, 
                                         @Param("radiusDegrees") Float radiusDegrees);

    /**
     * 查找城市类型的地理位置
     * 
     * @return 城市类型的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.locationType = 'CITY' ORDER BY g.locationName ASC")
    List<GeoLocation> findCities();

    /**
     * 查找省份类型的地理位置
     * 
     * @return 省份类型的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.locationType = 'PROVINCE' ORDER BY g.locationName ASC")
    List<GeoLocation> findProvinces();

    /**
     * 查找国家类型的地理位置
     * 
     * @return 国家类型的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.locationType = 'COUNTRY' ORDER BY g.locationName ASC")
    List<GeoLocation> findCountries();

    /**
     * 查找自然地理特征（山脉、河流、湖泊）
     * 
     * @return 自然地理特征列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.locationType IN ('MOUNTAIN', 'RIVER', 'LAKE') ORDER BY g.locationType ASC, g.locationName ASC")
    List<GeoLocation> findNaturalFeatures();

    /**
     * 查找历史建筑（宫殿、寺庙、陵墓）
     * 
     * @return 历史建筑列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.locationType IN ('PALACE', 'TEMPLE', 'TOMB') ORDER BY g.locationType ASC, g.locationName ASC")
    List<GeoLocation> findHistoricalBuildings();

    /**
     * 根据分析结果查找城市类型的地理位置
     * 
     * @param analysisResult 所属分析结果
     * @return 城市类型的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult = :analysisResult AND g.locationType = 'CITY' ORDER BY g.locationName ASC")
    List<GeoLocation> findCitiesByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 根据分析结果ID查找城市类型的地理位置
     * 
     * @param analysisId 分析结果ID
     * @return 城市类型的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.locationType = 'CITY' ORDER BY g.locationName ASC")
    List<GeoLocation> findCitiesByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 统计分析结果的地理位置数量
     * 
     * @param analysisResult 所属分析结果
     * @return 地理位置数量
     */
    long countByAnalysisResult(AnalysisResult analysisResult);

    /**
     * 统计分析结果的地理位置数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 地理位置数量
     */
    long countByAnalysisResultId(Long analysisId);

    /**
     * 统计指定类型的地理位置数量
     * 
     * @param locationType 位置类型
     * @return 地理位置数量
     */
    long countByLocationType(GeoLocation.LocationType locationType);

    /**
     * 统计分析结果中指定类型的地理位置数量
     * 
     * @param analysisResult 所属分析结果
     * @param locationType 位置类型
     * @return 地理位置数量
     */
    long countByAnalysisResultAndLocationType(AnalysisResult analysisResult, GeoLocation.LocationType locationType);

    /**
     * 统计分析结果中指定类型的地理位置数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @param locationType 位置类型
     * @return 地理位置数量
     */
    long countByAnalysisResultIdAndLocationType(Long analysisId, GeoLocation.LocationType locationType);

    /**
     * 统计有坐标信息的地理位置数量
     * 
     * @return 有坐标信息的地理位置数量
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    long countLocationsWithCoordinates();

    /**
     * 统计没有坐标信息的地理位置数量
     * 
     * @return 没有坐标信息的地理位置数量
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.latitude IS NULL OR g.longitude IS NULL")
    long countLocationsWithoutCoordinates();

    /**
     * 统计分析结果中有坐标信息的地理位置数量
     * 
     * @param analysisResult 所属分析结果
     * @return 有坐标信息的地理位置数量
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.analysisResult = :analysisResult AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    long countLocationsWithCoordinatesByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 统计分析结果中有坐标信息的地理位置数量（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 有坐标信息的地理位置数量
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    long countLocationsWithCoordinatesByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 获取分析结果的地理位置统计信息
     * 
     * @param analysisResult 所属分析结果
     * @return 地理位置总数
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.analysisResult = :analysisResult")
    Long getGeoLocationCount(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的地理位置统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 地理位置总数
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.analysisResult.id = :analysisId")
    Long getGeoLocationCountByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 获取坐标范围信息
     * 
     * @return 坐标范围信息数组 [最小纬度, 最大纬度, 最小经度, 最大经度]
     */
    @Query("SELECT MIN(g.latitude), MAX(g.latitude), MIN(g.longitude), MAX(g.longitude) FROM GeoLocation g WHERE g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<Object[]> getCoordinateRange();

    /**
     * 获取分析结果的坐标范围信息
     * 
     * @param analysisResult 所属分析结果
     * @return 坐标范围信息数组 [最小纬度, 最大纬度, 最小经度, 最大经度]
     */
    @Query("SELECT MIN(g.latitude), MAX(g.latitude), MIN(g.longitude), MAX(g.longitude) FROM GeoLocation g WHERE g.analysisResult = :analysisResult AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<Object[]> getCoordinateRangeByAnalysisResult(@Param("analysisResult") AnalysisResult analysisResult);

    /**
     * 获取分析结果的坐标范围信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 坐标范围信息数组 [最小纬度, 最大纬度, 最小经度, 最大经度]
     */
    @Query("SELECT MIN(g.latitude), MAX(g.latitude), MIN(g.longitude), MAX(g.longitude) FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL")
    List<Object[]> getCoordinateRangeByAnalysisId(@Param("analysisId") Long analysisId);

    /**
     * 根据用户查找地理位置（通过分析结果和项目关联）
     * 
     * @param userId 用户ID
     * @return 用户的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult.project.user.id = :userId ORDER BY g.locationName ASC")
    List<GeoLocation> findGeoLocationsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的地理位置数量
     * 
     * @param userId 用户ID
     * @return 地理位置数量
     */
    @Query("SELECT COUNT(g) FROM GeoLocation g WHERE g.analysisResult.project.user.id = :userId")
    long countGeoLocationsByUserId(@Param("userId") Long userId);

    /**
     * 根据分析结果ID删除地理位置数据
     * 
     * @param analysisResultId 分析结果ID
     */
    void deleteByAnalysisResultId(Long analysisResultId);

    /**
     * 根据分析结果ID分页查询地理位置
     * 
     * @param analysisId 分析结果ID
     * @param pageable 分页参数
     * @return 分页的地理位置列表
     */
    Page<GeoLocation> findByAnalysisResultId(Long analysisId, Pageable pageable);

    /**
     * 根据分析结果ID和位置类型分页查询地理位置
     * 
     * @param analysisId 分析结果ID
     * @param locationType 位置类型
     * @param pageable 分页参数
     * @return 分页的地理位置列表
     */
    Page<GeoLocation> findByAnalysisResultIdAndLocationType(Long analysisId, GeoLocation.LocationType locationType, Pageable pageable);

    /**
     * 根据分析结果ID和坐标范围分页查询地理位置
     * 
     * @param analysisId 分析结果ID
     * @param minLatitude 最小纬度
     * @param maxLatitude 最大纬度
     * @param minLongitude 最小经度
     * @param maxLongitude 最大经度
     * @param pageable 分页参数
     * @return 分页的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.latitude BETWEEN :minLatitude AND :maxLatitude AND g.longitude BETWEEN :minLongitude AND :maxLongitude")
    Page<GeoLocation> findByAnalysisResultIdAndLatitudeBetweenAndLongitudeBetween(@Param("analysisId") Long analysisId, 
                                                                                 @Param("minLatitude") double minLatitude, 
                                                                                 @Param("maxLatitude") double maxLatitude, 
                                                                                 @Param("minLongitude") double minLongitude, 
                                                                                 @Param("maxLongitude") double maxLongitude, 
                                                                                 Pageable pageable);

    /**
     * 根据分析结果ID查询附近的地理位置
     * 
     * @param analysisId 分析结果ID
     * @param centerLatitude 中心纬度
     * @param centerLongitude 中心经度
     * @param radiusKm 半径（公里）
     * @param pageable 分页参数
     * @return 附近的地理位置列表
     */
    @Query("SELECT g FROM GeoLocation g WHERE g.analysisResult.id = :analysisId AND g.latitude IS NOT NULL AND g.longitude IS NOT NULL AND " +
           "(6371 * acos(cos(radians(:centerLatitude)) * cos(radians(g.latitude)) * cos(radians(g.longitude) - radians(:centerLongitude)) + sin(radians(:centerLatitude)) * sin(radians(g.latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:centerLatitude)) * cos(radians(g.latitude)) * cos(radians(g.longitude) - radians(:centerLongitude)) + sin(radians(:centerLatitude)) * sin(radians(g.latitude))))")
    List<GeoLocation> findNearbyLocationsByAnalysis(@Param("analysisId") Long analysisId, 
                                                   @Param("centerLatitude") double centerLatitude, 
                                                   @Param("centerLongitude") double centerLongitude, 
                                                   @Param("radiusKm") double radiusKm, 
                                                   Pageable pageable);

    /**
     * 获取分析结果的地理位置统计信息（根据分析结果ID）
     * 
     * @param analysisId 分析结果ID
     * @return 地理位置统计信息列表
     */
    @Query("SELECT g.locationType, COUNT(g) FROM GeoLocation g WHERE g.analysisResult.id = :analysisId GROUP BY g.locationType")
    List<Object[]> getGeoLocationStatisticsByAnalysis(@Param("analysisId") Long analysisId);
}