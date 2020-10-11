# CustomDependencyInjection
CustomDependencyInjection is a small dependency injection (DI) library for java projects.

How to run the app?
In your main method call 'DIContext context = new DIContext().createContext(TestMain.class);'
Get the bean using 'context.getServiceInstance(VehicleService.class);'

"
	
	public static void main(String[] args) throws Exception {	
		DIContext context = new DIContext().createContext(TestMain.class);
		VehicleService vehicle = context.getServiceInstance(VehicleService.class);
		vehicle.service();
   }
"

Annotate your startup class with @Service

Supported annotations by default:

Service - Specify service.
Inject - Specify which constructor will be used to create instance of a service. also you can annotate fields with this annotation.
CustomQualifier - Specify the name of the dependency that you are requiring.
CustomScope - Specify the scope of the service. SINGLETON or PROTOTYPE.
