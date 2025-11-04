import com.example.demo.dtos.*;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${external.stock.url}")
    private String stockServiceUrl;

    @Value("${external.payment.url}")
    private String paymentServiceUrl;

    public OrderResponse createOrder(OrderRequest request) {
        long totalStart = System.currentTimeMillis();
        String threadInfo = Thread.currentThread().toString();
        
        log.info("🚀 [START] Order creation | Thread: {} | User: {}, Product: {}",
                threadInfo, request.getUserId(), request.getProductId());

        // 1. Création initiale en base (~50ms)
        long stepStart = System.currentTimeMillis();
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");
        
        order = orderRepository.save(order);
        long step1Duration = System.currentTimeMillis() - stepStart;
        log.info("📝 [STEP 1] DB Insert completed | Duration: {}ms | OrderId: {}", 
                step1Duration, order.getId());

        // 2. Vérification stock (~120ms via HTTP)
        stepStart = System.currentTimeMillis();
        log.info("🔍 [STEP 2] Checking stock...");
        
        StockResponse stockResponse = restTemplate.getForObject(
            stockServiceUrl + "/api/stock/check/" + request.getProductId(),
            StockResponse.class
        );
        
        long step2Duration = System.currentTimeMillis() - stepStart;
        log.info("📦 [STEP 2] Stock check completed | Duration: {}ms | Available: {}", 
                step2Duration, stockResponse.isAvailable());

        if (!stockResponse.isAvailable()) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            log.warn("❌ [FAILED] Order cancelled - Out of stock | OrderId: {}", order.getId());
            throw new RuntimeException("Out of stock");
        }

        // 3. Traitement paiement (~200ms via HTTP)
        stepStart = System.currentTimeMillis();
        log.info("💳 [STEP 3] Processing payment...");
        
        PaymentResponse paymentResponse = restTemplate.postForObject(
            paymentServiceUrl + "/api/payment/process",
            new Object(), // Payload simple pour le mock
            PaymentResponse.class
        );
        
        long step3Duration = System.currentTimeMillis() - stepStart;
        log.info("💰 [STEP 3] Payment completed | Duration: {}ms | PaymentId: {}", 
                step3Duration, paymentResponse.getPaymentId());

        if (!paymentResponse.isSuccess()) {
            order.setStatus("PAYMENT_FAILED");
            orderRepository.save(order);
            log.warn("❌ [FAILED] Payment failed | OrderId: {}", order.getId());
            throw new RuntimeException("Payment failed");
        }

        // 4. Mise à jour finale (~30ms)
        stepStart = System.currentTimeMillis();
        order.setStatus("CONFIRMED");
        order.setPaymentId(paymentResponse.getPaymentId());
        order = orderRepository.save(order);
        long step4Duration = System.currentTimeMillis() - stepStart;
        log.info("✅ [STEP 4] DB Update completed | Duration: {}ms", step4Duration);

        long totalDuration = System.currentTimeMillis() - totalStart;
        
        log.info("🎉 [SUCCESS] Order created | OrderId: {} | Total: {}ms | Breakdown: DB1={}ms, Stock={}ms, Payment={}ms, DB2={}ms",
                order.getId(), totalDuration, step1Duration, step2Duration, step3Duration, step4Duration);

        return new OrderResponse(
            order.getId(),
            order.getStatus(),
            order.getPaymentId(),
            totalDuration
        );
    }
}