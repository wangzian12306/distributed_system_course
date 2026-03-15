// 测试API按钮点击事件
document.getElementById('testApiBtn').addEventListener('click', function() {
    // 测试后端API
    fetch('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: 'testuser',
            password: 'password123',
            email: 'test@example.com'
        })
    })
    .then(response => response.json())
    .then(data => {
        console.log('API响应:', data);
        alert('API测试成功！');
    })
    .catch(error => {
        console.error('API测试失败:', error);
        alert('API测试失败，请检查后端服务是否运行');
    });
});

// 商品购买按钮点击事件
document.querySelectorAll('.product-item button').forEach(button => {
    button.addEventListener('click', function() {
        const productName = this.parentElement.querySelector('h4').textContent;
        alert(`您正在购买 ${productName}`);
    });
});

// 页面加载完成后执行
window.onload = function() {
    console.log('页面加载完成');
    // 可以在这里添加更多初始化代码
};