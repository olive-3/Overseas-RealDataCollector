package stock.overseas.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthenticationInfo {

    private String appKey;
    private String secretKey;
    private String grantType;
}
