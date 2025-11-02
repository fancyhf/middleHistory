#!/usr/bin/env python3
"""
完整的文件上传测试
模拟用户在前端上传文件的完整流程
"""

import requests
import json
import os

def test_complete_upload_flow():
    """测试完整的文件上传流程"""
    
    # 后端API地址
    base_url = "http://localhost:8080"
    upload_url = f"{base_url}/files/upload"
    
    # 测试文件路径
    test_file = "testdata/马可·波罗游记.doc"
    
    if not os.path.exists(test_file):
        print(f"❌ 测试文件不存在: {test_file}")
        return False
    
    try:
        print("🚀 开始完整的文件上传测试...")
        print("=" * 60)
        
        # 1. 模拟前端文件上传
        print("1️⃣ 模拟前端文件上传...")
        with open(test_file, 'rb') as f:
            files = {
                'file': (os.path.basename(test_file), f, 'application/msword')
            }
            data = {
                'projectId': '1'
            }
            
            print(f"📤 上传文件: {test_file}")
            response = requests.post(upload_url, files=files, data=data)
            
            print(f"📊 响应状态码: {response.status_code}")
            
            if response.status_code == 200:
                result = response.json()
                print(f"✅ 上传成功!")
                
                # 2. 验证响应数据结构
                print("\n2️⃣ 验证响应数据结构...")
                print("📄 完整响应数据:")
                print(json.dumps(result, indent=2, ensure_ascii=False))
                
                # 检查响应格式
                if 'data' in result and 'success' in result:
                    file_data = result['data']
                    print("✅ 响应格式正确 (包含 data 和 success 字段)")
                    
                    # 3. 验证前端需要的所有字段
                    print("\n3️⃣ 验证前端需要的字段...")
                    required_fields = {
                        'id': '文件ID',
                        'fileName': '文件名',
                        'originalFileName': '原始文件名',
                        'fileType': '文件类型',
                        'fileSize': '文件大小',
                        'formattedSize': '格式化文件大小',
                        'status': '状态',
                        'processStatus': '处理状态',
                        'projectId': '项目ID',
                        'uploadTime': '上传时间',
                        'canAnalyze': '可分析标志'
                    }
                    
                    missing_fields = []
                    present_fields = []
                    
                    for field, description in required_fields.items():
                        if field in file_data:
                            present_fields.append(f"✅ {field} ({description}): {file_data[field]}")
                        else:
                            missing_fields.append(f"❌ {field} ({description})")
                    
                    print("存在的字段:")
                    for field in present_fields:
                        print(f"  {field}")
                    
                    if missing_fields:
                        print("\n缺少的字段:")
                        for field in missing_fields:
                            print(f"  {field}")
                        return False
                    else:
                        print("\n✅ 所有必需字段都存在!")
                        
                        # 4. 验证字段值的合理性
                        print("\n4️⃣ 验证字段值的合理性...")
                        
                        # 检查文件大小
                        actual_size = os.path.getsize(test_file)
                        if file_data['fileSize'] == actual_size:
                            print(f"✅ 文件大小正确: {file_data['fileSize']} bytes")
                        else:
                            print(f"❌ 文件大小不匹配: 期望 {actual_size}, 实际 {file_data['fileSize']}")
                            return False
                        
                        # 检查文件名
                        expected_filename = os.path.basename(test_file)
                        if file_data['originalFileName'] == expected_filename:
                            print(f"✅ 原始文件名正确: {file_data['originalFileName']}")
                        else:
                            print(f"❌ 原始文件名不匹配: 期望 {expected_filename}, 实际 {file_data['originalFileName']}")
                            return False
                        
                        # 检查状态
                        if file_data['status'] in ['UPLOADED', 'PROCESSING', 'PROCESSED']:
                            print(f"✅ 文件状态正确: {file_data['status']}")
                        else:
                            print(f"❌ 文件状态异常: {file_data['status']}")
                            return False
                        
                        # 检查项目ID
                        if str(file_data['projectId']) == '1':
                            print(f"✅ 项目ID正确: {file_data['projectId']}")
                        else:
                            print(f"❌ 项目ID不匹配: 期望 1, 实际 {file_data['projectId']}")
                            return False
                        
                        print("\n🎉 完整的文件上传测试通过!")
                        print("✅ 前端文件上传功能已完全修复!")
                        return True
                        
                else:
                    print("❌ 响应格式错误 (缺少 data 或 success 字段)")
                    return False
            else:
                print(f"❌ 上传失败: {response.status_code}")
                print(f"📄 错误响应: {response.text}")
                return False
                
    except Exception as e:
        print(f"❌ 测试过程中发生错误: {str(e)}")
        return False

if __name__ == "__main__":
    success = test_complete_upload_flow()
    if success:
        print("\n" + "=" * 60)
        print("🎊 恭喜！文件上传功能已完全修复并正常工作！")
        print("🌐 用户现在可以在 http://localhost:3000 正常上传文件了！")
    else:
        print("\n" + "=" * 60)
        print("❌ 文件上传功能仍有问题，需要进一步调试。")
        exit(1)