# kabouros-jdbc

Simple and light weight object relational mapping

The simple kabouros-jdbc configuration with Java-Config looks like this:

```java
import com.firecode.kabouros.jdbc.RepositoryProxyFactory;
import com.firecode.kabouros.jdbc.starter.RelationalRepositoryProxyFactory;
import com.firecode.kabouros.jdbc.starter.annotation.EnableRepositoryScan;


@Configuration
@EnableRepositoryScan
public class ApplicationConfig {
  @Bean
  public DataSource dataSource() {
    //....
  }
  
  @Bean
  public PlatformTransactionManager defaultTransactionManager() {
    //...
  }

  @Bean
  public RepositoryProxyFactory repositoryProxyFactory(){
    RelationalRepositoryProxyFactory factory = new RelationalRepositoryProxyFactory();
	factory.setDataSource(dataSource);
	return factory;
  }
}
```

Create an entity:

```java
import com.firecode.kabouros.common.annotation.GeneratedValue;
import com.firecode.kabouros.jdbc.annotation.Id;
import com.firecode.kabouros.jdbc.annotation.Table;

@Table(name="xxx")
public class User {

  @Id
  @GeneratedValue
  private Long id;
  private String name;
       
  //Getters and setters
}
```

Create an interface:

```java
import com.firecode.kabouros.jdbc.query.Repository
import com.firecode.kabouros.jdbc.annotation.EnableRepository;


@EnableRepository
public interface UserRepository extends Repository<User> {
  int save(User... users);
  User findByname(String name);
  int deleteById(Long id);
}
```

Write a spring-boot client:
```java


@SpringBootApplication
@RestController
public class Application {
	
  @Autowired
  private UserRepository repository; 
	 
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
	
  @GetMapping("/test")
  @Transactional(rollbackFor=Exception.class)
  public void testName(){
    User jiang = new User("jiang");
	User zhang = new User("zhang");
	repository.save(jiang,zhang);
	User user = repository.findByname("jiang");
    repository.deleteById(24576827557154816L);
  }

}
```

# The use Eclipse needs to be open the parameter compiler.
window ->  preferences -> java -> compiler -> [Check]{store information about method parameters(usable via reflection)}





