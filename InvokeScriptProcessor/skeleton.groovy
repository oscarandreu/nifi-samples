class E{ void executeScript(session, context, log, REL_SUCCESS, REL_FAILURE) 
    {
        def flowFile = session.get()
        if (!flowFile) {
            return
        }

        flowFile = session.putAttribute(flowFile, "FOO", "BAR")    
        // transfer
        session.transfer(flowFile, REL_SUCCESS) 
    }
}

class GroovyProcessor implements Processor {
    def REL_SUCCESS = new Relationship.Builder().name("success").description('FlowFiles that were successfully processed are routed here').build()
    def REL_FAILURE = new Relationship.Builder().name("failure").description('FlowFiles that were not successfully processed are routed here').build()
    def ComponentLog log
    def e = new E()   

    void initialize(ProcessorInitializationContext context) { 
        log = context.logger 
    }

    Set<Relationship> getRelationships() { 
        return [REL_FAILURE, REL_SUCCESS] as Set 
    }

    Collection<ValidationResult> validate(ValidationContext context) { null }
    PropertyDescriptor getPropertyDescriptor(String name) { null }
    void onPropertyModified(PropertyDescriptor descriptor, String oldValue, String newValue) { }
    List<PropertyDescriptor> getPropertyDescriptors() { null }
    String getIdentifier() { null }    
    
    void onTrigger(ProcessContext context, ProcessSessionFactory sessionFactory) throws ProcessException {
        def session = sessionFactory.createSession()
        try {
            e.executeScript(session, context, log, REL_SUCCESS, REL_FAILURE)
            session.commit()
        } 
        catch (final Throwable t) {
            log.error('{} failed to process due to {}; rolling back session', [this, t] as Object[])
            session.rollback(true)
            throw t
        }
    }
}

processor = new GroovyProcessor()
