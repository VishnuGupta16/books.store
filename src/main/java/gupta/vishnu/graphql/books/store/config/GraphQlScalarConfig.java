package gupta.vishnu.graphql.books.store.config;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static graphql.parser.antlr.GraphqlParser.StringValue;

@Configuration
public class GraphQlScalarConfig {

    private static  final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Bean
    public RuntimeWiringConfigurer dateScalarConfigurer() {
        return wiring -> wiring.scalar(dateScalar());
    }

    private GraphQLScalarType dateScalar() {
        return GraphQLScalarType.newScalar()
                .name("Date")
                .description("ISO-8601 LocalDateTime")
                .coercing(new Coercing<LocalDateTime, String>() {

                    @Override  // Java -> output
                    public String serialize(Object input, GraphQLContext ctx, Locale locale) {
                        if (input instanceof LocalDateTime ldt) return ldt.format(ISO);
                        throw new CoercingSerializeException("Expected LocalDateTime, got: " + input);
                    }

                    @Override
                    public LocalDateTime parseValue(Object input, GraphQLContext ctx, Locale locale) {
                        try {
                            return LocalDateTime.parse(input.toString(), ISO);
                        } catch (Exception exception){
                            throw new CoercingParseValueException("Invalid Date: " + input, exception);
                        }
                    }

                    @Override
                    public @Nullable LocalDateTime parseLiteral(@NonNull Value<?> input, @NonNull CoercedVariables variables, @NonNull GraphQLContext graphQLContext, @NonNull Locale locale) throws CoercingParseLiteralException {
                        if (input instanceof StringValue sv) {
                            try {
                                assert sv.getValue() != null;
                                return LocalDateTime.parse(sv.getValue(), ISO);
                            } catch (Exception exception) {
                                throw new CoercingParseLiteralException("Invalid Date: " + input, exception);
                            }
                        } else {
                            throw new CoercingParseLiteralException("Expected a String literal for Date");
                        }
                    }
                })
                .build();
    }
}
