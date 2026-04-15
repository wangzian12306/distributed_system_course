// ==================== 全局变量 ====================
let currentUser = null;
let token = null;
let currentPage = 'home';

// ==================== 工具函数 ====================
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert ${type}`;
    alertDiv.textContent = message;
    
    const main = document.querySelector('main');
    main.insertBefore(alertDiv, main.firstChild);
    
    setTimeout(() => {
        alertDiv.remove();
    }, 3000);
}

function showLoading(containerId) {
    const container = document.getElementById(containerId);
    if (container) {
        container.innerHTML = '<div class="loading">加载中...</div>';
    }
}

async function fetchAPI(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
        const response = await fetch(url, {
            ...options,
            headers
        });
        
        // 检查X-Instance-Id响应头
        const instanceId = response.headers.get('X-Instance-Id');
        if (instanceId) {
            document.getElementById('instance-id').textContent = instanceId;
            document.getElementById('footer-instance-id').textContent = instanceId;
            document.getElementById('instance-info').style.display = 'block';
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API请求失败:', error);
        throw error;
    }
}

// ==================== 用户认证 ====================
function initAuth() {
    const savedToken = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (savedToken && savedUser) {
        token = savedToken;
        currentUser = JSON.parse(savedUser);
        updateAuthUI();
    }
}

function updateAuthUI() {
    const authNav = document.getElementById('auth-nav');
    const userNav = document.getElementById('user-nav');
    const welcomeMsg = document.getElementById('welcome-msg');
    
    if (currentUser) {
        authNav.style.display = 'none';
        userNav.style.display = 'inline';
        welcomeMsg.textContent = `欢迎, ${currentUser.username}`;
    } else {
        authNav.style.display = 'inline';
        userNav.style.display = 'none';
    }
}

function showLoginModal() {
    document.getElementById('login-modal').classList.add('show');
}

function hideLoginModal() {
    document.getElementById('login-modal').classList.remove('show');
}

function showRegisterModal() {
    document.getElementById('register-modal').classList.add('show');
}

function hideRegisterModal() {
    document.getElementById('register-modal').classList.remove('show');
}

async function login(username, password) {
    try {
        const result = await fetchAPI('/api/users/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        if (result.code === 200) {
            token = result.data.token;
            currentUser = result.data.user;
            localStorage.setItem('token', token);
            localStorage.setItem('user', JSON.stringify(currentUser));
            updateAuthUI();
            hideLoginModal();
            showAlert('登录成功！', 'success');
        } else {
            showAlert(result.message || '登录失败', 'error');
        }
    } catch (error) {
        showAlert('登录失败，请检查网络连接', 'error');
    }
}

async function register(username, email, password) {
    try {
        const result = await fetchAPI('/api/users/register', {
            method: 'POST',
            body: JSON.stringify({ username, email, password })
        });
        
        if (result.code === 200) {
            showAlert('注册成功！请登录', 'success');
            hideRegisterModal();
            showLoginModal();
        } else {
            showAlert(result.message || '注册失败', 'error');
        }
    } catch (error) {
        showAlert('注册失败，请检查网络连接', 'error');
    }
}

function logout() {
    token = null;
    currentUser = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    updateAuthUI();
    showAlert('已退出登录', 'info');
    navigateTo('home');
}

// ==================== 页面导航 ====================
function navigateTo(page) {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.remove('active');
        if (link.dataset.page === page) {
            link.classList.add('active');
        }
    });
    
    document.querySelectorAll('.page').forEach(p => {
        p.classList.remove('active');
    });
    
    const targetPage = document.getElementById(`page-${page}`);
    if (targetPage) {
        targetPage.classList.add('active');
    }
    
    currentPage = page;
    
    // 加载页面数据
    switch (page) {
        case 'home':
            loadHotProducts();
            break;
        case 'products':
            loadAllProducts();
            break;
        case 'seckill':
            loadSeckillProducts();
            break;
        case 'orders':
            if (currentUser) {
                loadOrders();
            } else {
                showAlert('请先登录', 'info');
                showLoginModal();
            }
            break;
    }
}

// ==================== 商品相关 ====================
function renderProductItem(product, showSeckill = false) {
    return `
        <div class="product-item" data-id="${product.id}">
            <h4>${product.name}</h4>
            <p class="description">${product.description || '暂无描述'}</p>
            <div class="stock-info">
                库存: ${product.stock}
                ${showSeckill && product.seckillStock > 0 ? 
                    `<span class="seckill"> | 秒杀库存: ${product.seckillStock}</span>` : ''}
            </div>
            <p class="price">¥${product.price.toFixed(2)}</p>
            <div class="button-group">
                <button onclick="showProductDetail(${product.id})">查看详情</button>
                <button onclick="buyProduct(${product.id})">立即购买</button>
                ${showSeckill && product.seckillStock > 0 ? 
                    `<button class="seckill-btn" onclick="seckillProduct(${product.id})">秒杀</button>` : ''}
            </div>
        </div>
    `;
}

async function loadHotProducts() {
    showLoading('hot-products');
    try {
        const result = await fetchAPI('/api/products');
        if (result.code === 200) {
            const products = result.data.slice(0, 6);
            document.getElementById('hot-products').innerHTML = 
                products.map(p => renderProductItem(p)).join('');
        }
    } catch (error) {
        document.getElementById('hot-products').innerHTML = 
            '<div class="loading">加载失败</div>';
    }
}

async function loadAllProducts() {
    showLoading('all-products');
    try {
        const result = await fetchAPI('/api/products');
        if (result.code === 200) {
            document.getElementById('all-products').innerHTML = 
                result.data.map(p => renderProductItem(p, true)).join('');
        }
    } catch (error) {
        document.getElementById('all-products').innerHTML = 
            '<div class="loading">加载失败</div>';
    }
}

async function loadSeckillProducts() {
    showLoading('seckill-products');
    try {
        const result = await fetchAPI('/api/products');
        if (result.code === 200) {
            const seckillProducts = result.data.filter(p => p.seckillStock > 0);
            document.getElementById('seckill-products').innerHTML = 
                seckillProducts.map(p => renderProductItem(p, true)).join('');
        }
    } catch (error) {
        document.getElementById('seckill-products').innerHTML = 
            '<div class="loading">加载失败</div>';
    }
}

async function showProductDetail(productId) {
    try {
        const result = await fetchAPI(`/api/products/${productId}`);
        if (result.code === 200) {
            const product = result.data;
            document.getElementById('product-detail-content').innerHTML = `
                <div class="product-detail-content">
                    <h3>${product.name}</h3>
                    <p class="desc">${product.description || '暂无描述'}</p>
                    <p class="price">¥${product.price.toFixed(2)}</p>
                    <p class="stock">库存: ${product.stock} | 秒杀库存: ${product.seckillStock}</p>
                    <div class="actions">
                        <button class="buy-btn" onclick="buyProduct(${product.id}); hideProductModal();">立即购买</button>
                        ${product.seckillStock > 0 ? 
                            `<button class="seckill-btn" onclick="seckillProduct(${product.id}); hideProductModal();">秒杀</button>` : ''}
                    </div>
                </div>
            `;
            document.getElementById('product-modal').classList.add('show');
        }
    } catch (error) {
        showAlert('获取商品详情失败', 'error');
    }
}

function hideProductModal() {
    document.getElementById('product-modal').classList.remove('show');
}

async function buyProduct(productId) {
    if (!currentUser) {
        showAlert('请先登录', 'info');
        showLoginModal();
        return;
    }
    
    try {
        const result = await fetchAPI('/api/orders', {
            method: 'POST',
            body: JSON.stringify({
                userId: currentUser.id,
                productId: productId,
                quantity: 1
            })
        });
        
        if (result.code === 200) {
            showAlert('订单创建成功！', 'success');
            if (currentPage === 'orders') {
                loadOrders();
            } else {
                navigateTo('orders');
            }
        } else {
            showAlert(result.message || '购买失败', 'error');
        }
    } catch (error) {
        showAlert('购买失败，请检查网络连接', 'error');
    }
}

async function seckillProduct(productId) {
    if (!currentUser) {
        showAlert('请先登录', 'info');
        showLoginModal();
        return;
    }
    
    try {
        const result = await fetchAPI(`/api/orders/seckill?userId=${currentUser.id}&productId=${productId}`, {
            method: 'POST'
        });
        
        if (result.code === 200) {
            showAlert('秒杀成功！', 'success');
            if (currentPage === 'orders') {
                loadOrders();
            } else {
                navigateTo('orders');
            }
            // 刷新商品列表
            if (currentPage === 'seckill') {
                loadSeckillProducts();
            } else if (currentPage === 'products') {
                loadAllProducts();
            } else if (currentPage === 'home') {
                loadHotProducts();
            }
        } else {
            showAlert(result.message || '秒杀失败', 'error');
        }
    } catch (error) {
        showAlert('秒杀失败，请检查网络连接', 'error');
    }
}

// ==================== 搜索功能 ====================
async function searchProducts(keyword) {
    if (!keyword.trim()) {
        if (currentPage === 'home') {
            loadHotProducts();
        } else if (currentPage === 'products') {
            loadAllProducts();
        } else if (currentPage === 'seckill') {
            loadSeckillProducts();
        }
        return;
    }
    
    try {
        const result = await fetchAPI(`/api/search?keyword=${encodeURIComponent(keyword)}`);
        if (result.code === 200) {
            const containerId = currentPage === 'home' ? 'hot-products' : 
                              currentPage === 'products' ? 'all-products' : 'seckill-products';
            document.getElementById(containerId).innerHTML = 
                result.data.map(p => renderProductItem(p, true)).join('');
        }
    } catch (error) {
        showAlert('搜索失败', 'error');
    }
}

// ==================== 订单相关 ====================
function getOrderStatusText(status) {
    const statusMap = {
        0: '待支付',
        1: '已支付',
        2: '已取消'
    };
    return statusMap[status] || '未知';
}

function getOrderStatusClass(status) {
    const classMap = {
        0: 'pending',
        1: 'paid',
        2: 'cancelled'
    };
    return classMap[status] || '';
}

function renderOrderItem(order) {
    return `
        <div class="order-item">
            <div class="order-header">
                <span class="order-id">订单号: ${order.id}</span>
                <span class="order-status ${getOrderStatusClass(order.status)}">
                    ${getOrderStatusText(order.status)}
                </span>
            </div>
            <div class="order-details">
                <div class="order-detail-item">
                    <strong>商品ID:</strong> ${order.productId}
                </div>
                <div class="order-detail-item">
                    <strong>数量:</strong> ${order.quantity}
                </div>
                <div class="order-detail-item">
                    <strong>总价:</strong> ¥${order.totalPrice.toFixed(2)}
                </div>
                <div class="order-detail-item">
                    <strong>创建时间:</strong> ${new Date(order.createTime).toLocaleString()}
                </div>
            </div>
            ${order.status === 0 ? `
                <div class="order-actions">
                    <button onclick="payOrder(${order.id})">立即支付</button>
                </div>
            ` : ''}
        </div>
    `;
}

async function loadOrders() {
    if (!currentUser) return;
    
    showLoading('orders-list');
    try {
        const result = await fetchAPI(`/api/orders/user/${currentUser.id}`);
        if (result.code === 200) {
            if (result.data && result.data.length > 0) {
                document.getElementById('orders-list').innerHTML = 
                    result.data.map(o => renderOrderItem(o)).join('');
            } else {
                document.getElementById('orders-list').innerHTML = 
                    '<div class="loading">暂无订单</div>';
            }
        }
    } catch (error) {
        document.getElementById('orders-list').innerHTML = 
            '<div class="loading">加载失败</div>';
    }
}

async function payOrder(orderId) {
    try {
        const result = await fetchAPI(`/api/payments/pay/${orderId}`, {
            method: 'POST'
        });
        
        if (result.code === 200) {
            showAlert('支付成功！', 'success');
            loadOrders();
        } else {
            showAlert(result.message || '支付失败', 'error');
        }
    } catch (error) {
        showAlert('支付失败，请检查网络连接', 'error');
    }
}

// ==================== 事件绑定 ====================
function bindEvents() {
    // 导航链接
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            navigateTo(link.dataset.page);
        });
    });
    
    // 登录/注册链接
    document.getElementById('login-link').addEventListener('click', (e) => {
        e.preventDefault();
        showLoginModal();
    });
    
    document.getElementById('register-link').addEventListener('click', (e) => {
        e.preventDefault();
        showRegisterModal();
    });
    
    document.getElementById('logout-link').addEventListener('click', (e) => {
        e.preventDefault();
        logout();
    });
    
    // 弹窗关闭
    document.querySelectorAll('.close').forEach(closeBtn => {
        closeBtn.addEventListener('click', () => {
            hideLoginModal();
            hideRegisterModal();
            hideProductModal();
        });
    });
    
    // 点击弹窗外部关闭
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('show');
            }
        });
    });
    
    // 登录表单
    document.getElementById('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        login(username, password);
    });
    
    // 注册表单
    document.getElementById('register-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const username = document.getElementById('register-username').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;
        register(username, email, password);
    });
    
    // 切换登录/注册
    document.getElementById('show-register').addEventListener('click', (e) => {
        e.preventDefault();
        hideLoginModal();
        showRegisterModal();
    });
    
    document.getElementById('show-login').addEventListener('click', (e) => {
        e.preventDefault();
        hideRegisterModal();
        showLoginModal();
    });
    
    // 搜索功能
    document.getElementById('search-btn').addEventListener('click', () => {
        const keyword = document.getElementById('search-input').value;
        searchProducts(keyword);
    });
    
    document.getElementById('search-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            const keyword = document.getElementById('search-input').value;
            searchProducts(keyword);
        }
    });
}

// ==================== 初始化 ====================
window.onload = function() {
    console.log('页面加载完成');
    initAuth();
    bindEvents();
    loadHotProducts();
};
